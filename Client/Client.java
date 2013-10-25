package Client;

import Domain.Article;
import Domain.BulletinBoard;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.TreeMultimap;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/**
 * @author Chris Card, Steven Rupert
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
        BulletinBoard server = (BulletinBoard) reg.lookup("BulletinBoard");

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
            //int offset = Integer.parseInt(args[1]);

            List<Article> articles = server.getArticles(); // TODO offset and limit to 10

            Map<Integer, Article> articlesById = Maps.uniqueIndex(articles, new Function<Article, Integer>()
            {
                @Override
                public Integer apply(Domain.Article article)
                {
                    return article.id;
                }
            });

            // index replies
            TreeMultimap<Article, Article> replies = TreeMultimap.create();
            for (Article article : articles)
            {
                if (article.id != article.parent && articlesById.containsKey(article.parent))
                {
                    replies.put(articlesById.get(article.parent), article);
                }
            }

            Deque<Deque<Article>> stack = Queues.newArrayDeque();
            stack.push(Queues.<Article>newArrayDeque());

            // add root articles (reply to 0)
            for (Article article : articles)
            {
                if (article.parent == 0)
                {
                    stack.peek().add(article);
                }
            }

            // list articles by id and reply order (preorder traversal)
            while (stack.size() > 0 && stack.peek().size() > 0)
            {
                Article a = stack.peek().pop();
                System.out.println(
                        String.format("%sArticle %s: %s%s",
                                Strings.repeat("  ", stack.size() - 1),
                                a.id,
                                a.content.substring(0, Math.min(a.content.length(), 50)),
                                a.content.length() > 50 ? "..." : ""));

                stack.push(Queues.newArrayDeque(replies.get(a)));
                while (stack.size() > 0 && stack.peek().isEmpty())
                {
                    stack.pop();
                }
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
