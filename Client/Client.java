package Client;

import Domain.Article;
import Domain.BulletinBoard;
import Domain.ConsistencyLevel;
import com.beust.jcommander.*;
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
    public String host;

    @Parameter(names = "-port",
               description = "Port of BulletinBoard server to which to connect.",
               required = true)
    public int port;

    @Parameters(commandDescription = "Post a new article or reply to an existing article. " +
                                     "Article content is read from STDIN.")
    private static class PostCommand
    {
        @Parameter(names = {"-reply"},
                description = "If replying to an existing article, the ID of that article. " +
                              "Default behaviour (0) is a top-level article.")
        public int replyId = 0;

        @Parameter(names = "-consistency",
                   description = "Desired write consistency, either ALL, QUORUM, or ONE. " +
                                 "See explanation of consistency below.",
                   converter = ConsistencyConverter.class)
        public ConsistencyLevel consistency = ConsistencyLevel.QUORUM;
    }

    private static class ReadCommand {
        @Parameter(names = "-consistency",
                   description = "Desired read consistency, either ALL, QUORUM, or ONE. " +
                              "See explanation of consistency below.",
                   converter = ConsistencyConverter.class)
        public ConsistencyLevel consistency = ConsistencyLevel.QUORUM;
    }

    @Parameters(commandDescription = "List the 10 latest articles on the server.")
    private static class ListCommand extends ReadCommand
    {
        @Parameter(names = {"-offset"},
                description = "ID from which to start listing articles. At most 10 articles" +
                              " are listed from this offset.")
        public int offset = 0;
    }

    @Parameters(commandDescription = "Get a specific article from the server.")
    private static class GetCommand extends ReadCommand
    {
        @Parameter(names = {"-id"},
                   description = "ID of the article to get.",
                   required = true)
        public int id;
    }

    private static class ConsistencyConverter implements IStringConverter<ConsistencyLevel> {

        @Override
        public ConsistencyLevel convert(String s)
        {
            try
            {
                return ConsistencyLevel.valueOf(s);
            } catch (IllegalArgumentException e)
            {
                throw new ParameterException("Consistency level " + s + " not recognized.");
            }
        }
    }

    public static final String HELP =
            "Consistency explained:\n" +
            "- ALL: Writes will wait for all nodes in the cluster to respond before returning,\n" +
            "       i.e if the write succeeds, ALL nodes have replicated the write.\n" +
            "       Reads will represent latest known article(s) of ALL nodes.\n" +
            "- QUORUM: Writes will wait for n+2/1 nodes in the cluster to respond,\n" +
            "          i.e if the write succeeds, a majority of nodes have replicated the write.\n" +
            "          Reads will represent latest known article(s) in n/2 nodes in the cluster.\n" +
            "          If using QUORUM write consistency, QUORUM read consistency must be used to\n" +
            "          guarantee that writes will be visible in the read.\n" +
            "- ONE: Writes will wait for one node in the cluster to respond.\n" +
            "       Reads will represent the article(s) known to the first server to respond.\n" +
            "       If ONE write consistency is used, then ALL read consistency must be used to\n" +
            "       ensure writes will be visible. ALL writes can safely be read with ONE reads\n" +
            "       (sequential consistency).\n\n" +
            "Example usage:\n\n" +
            "List articles:\n" +
            "    ./client.sh -host localhost -port 5555 list -consistency ALL\n" +
            "Get article:\n" +
            "    ./client.sh -host localhost -port 5555 get -id 5 -consistency QUORUM\n" +
            "Post article:\n" +
            "    ./client.sh -host localhost -port 5555 post -reply -consistency ONE 1 \\\n" +
            "                < hot-opinions.txt\n";

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
            System.err.println(HELP);
            System.exit(255);
        }

        // connect to server
        Registry reg = LocateRegistry.getRegistry(client.host, client.port);
        BulletinBoard server = (BulletinBoard) reg.lookup("BulletinBoard");

        if ("post".equals(jCommander.getParsedCommand()))
        {

            // slurp STDIN
            Scanner scanner = new Scanner(System.in).useDelimiter("\\Z");
            String content = scanner.nextLine();
            scanner.close();

            Article article = new Article(content, postCommand.replyId);

            server.post(article, postCommand.consistency);

            System.err.println("Article posted.");
        }
        else if ("list".equals(jCommander.getParsedCommand()))
        {
            List<Article> articles = server.getArticles(listCommand.offset,
                                                        listCommand.consistency);

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
            Article article = server.choose(getCommand.id, getCommand.consistency);
            System.out.println(String.format("Article %s\n%s", article.id, article.content));
        }
        else
        {
            jCommander.usage();
            System.err.println(HELP);
            System.exit(255);
        }
    }
}