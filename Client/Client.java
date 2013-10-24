package Client;

import Compute.Article;
import Compute.BulletinBoard;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/**
 * @Author Chris Card, Steven Rupert
 * Bulletin Board client program. Reads and writes articles hosted on the distributed
 * Bulletin Board system.
 */
public class Client
{

    public static final String USAGE =
            "Usage: COMMAND [REPLY_ID|OFFSET|ARTICLE_ID] [SERVER HOST] [SERVER PORT]\n" +
            "  COMMAND: Either POST, LIST, or GET, case insensitive\n" +
            "      POST: post a new article. Content is read from STDIN.\n" +
            "            REPLY_ID _must_ be present, and equal to 0 if not replying\n" +
            "            to an existing post.\n" +
            "      LIST: list articles on the server, up to 10 at a time.\n" +
            "            OFFSET must be present. Use\n" +
            "            0 to read articles from the beginning.\n" +
            "      GET: get a specific article on the server. ARTICLE_ID must\n" +
            "           be present.";

    /**
     * Sends a command to a random server in the cluster and writes the response to STDOUT.
     */
    public static void main(String args[]) throws Throwable
    {
        if (args.length != 4)
        {
            System.err.println(USAGE);
            System.exit(255);
        }

        final String host = args[2];
        final int port = Integer.parseInt(args[3]);

        // connect to server
        Registry reg = LocateRegistry.getRegistry(host, port);
        BulletinBoard server = (BulletinBoard) reg.lookup("Compute");

        final String command = args[0];
        if ("post".equals(command.toLowerCase()))
        {
            int parent = Integer.parseInt(args[1]);

            // slurp STDIN
            Scanner scanner = new Scanner(System.in).useDelimiter("\\Z");
            String content = scanner.nextLine();
            scanner.close();

            Article article = new Article(content, parent);

            server.post(article);

            System.err.println("Article posted.");
        }
        else if ("list".equals(command.toLowerCase()))
        {
            int offset = Integer.parseInt(args[1]);

            List<Article> articles = server.getArticles(); // TODO offset and limit to 10
            // index by id
            Map<Integer, Article> articlesById = Maps.uniqueIndex(articles, new Function<Article, Integer>()
            {
                @Override
                public Integer apply(Compute.Article article)
                {
                    return article.id;
                }
            });
            for (Article article : articles)
            {
                // display excerpt
                StringBuilder indent = new StringBuilder();
                int parent = article.parent;
                while (parent != 0)
                {
                    if (articlesById.containsKey(parent))
                    {
                        indent.append("  ");
                        parent = articlesById.get(parent).parent;
                    } else
                    {
                        break;
                    }
                }
                System.out.println(
                        String.format("%sArticle %s: %s%s",
                                indent.toString(),
                                article.id,
                                article.content.substring(
                                        0,
                                        Math.min(article.content.length(), 100)),
                                article.content.length() > 100 ? "..." : ""));
            }
        }
        else if ("get".equals(command.toLowerCase()))
        {
            Article article = server.choose(Integer.parseInt(args[1]));
            System.out.println(String.format("Article %s\n%s", article.id, article.content));
        }
        else
        {
            System.err.println(USAGE);
            System.exit(255);
        }
    }
}
