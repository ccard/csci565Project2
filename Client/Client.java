package Client;

import Domain.Article;
import Domain.BulletinBoard;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
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
    @Parameter(names = "-host",
               description = "Hostname of BulletinBoard server to which to connect.",
               required = true)
    private String host;

    @Parameter(names = "-port",
               description = "Port of BulletinBoard server to which to connect.",
               required = true)
    private int port;

    @Parameters(commandDescription = "Post a new article or reply to an existing article. " +
                                     "Article content is read from STDIN.")
    private static class PostCommand
    {
        @Parameter(names = {"-reply"},
                description = "If replying to an existing article, the ID of that article.")
        private int replyId = 0;
    }

    @Parameters(commandDescription = "List the 10 latest articles on the server.")
    private static class ListCommand
    {
        @Parameter(names = {"-offset"},
                description = "ID from which to start listing articles. At most 10 articles" +
                              " are listed from this offset.")
        private int offset = 0;
    }

    @Parameters(commandDescription = "Get a specific article from the server.")
    private static class GetCommand
    {
        @Parameter(names = {"-id"},
                   description = "ID of the article to get.",
                   required = true)
        private int id;
    }

    /**
     * Sends a command to a random server in the cluster and writes the response to STDOUT.
     */
    public static void main(String args[]) throws Throwable
    {
        Client client = new Client();
        JCommander jCommander = new JCommander(client);
        jCommander.setProgramName("client.sh");
        PostCommand postCommand = new PostCommand();
        jCommander.addCommand("post", postCommand);
        ListCommand listCommand = new ListCommand();
        jCommander.addCommand("list", listCommand);
        GetCommand getCommand = new GetCommand();
        jCommander.addCommand("get", getCommand);

        try
        {
            jCommander.parse(args);
        } catch (ParameterException e)
        {
            System.err.println(e.getMessage() + "\n");
            jCommander.usage();
            System.exit(255);
        }

        // connect to server
        Registry reg = LocateRegistry.getRegistry(client.host, client.port);
        BulletinBoard server = (BulletinBoard) reg.lookup("BulletinBoard");

        final String command = args[0];
        if ("post".equals(jCommander.getParsedCommand()))
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
        else if ("list".equals(jCommander.getParsedCommand()))
        {
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
        else if ("get".equals(jCommander.getParsedCommand()))
        {
            Article article = server.choose(Integer.parseInt(args[1]));
            System.out.println(String.format("Article %s\n%s", article.id, article.content));
        }
        else
        {
            jCommander.usage();
            System.exit(255);
        }
    }
}