/**
 * @Author Chris Card, Steven  Rupert
 * This is the local data base store for each server
 */
package Server;

import Domain.Article;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Database-backed possibly persistent article store.
 * <p/>
 * Thread-safety is dictated by the backing database--practically, instances will
 * be thread safe thanks to JDBI/H2.
 */
@RegisterMapper(ArticleStore.ArticleMapper.class)
public interface ArticleStore
{
    @SqlUpdate("create table if not exists " +
               "articles (id int primary key, content text, parent int)")
    void initializeTable();

    // other_column is necessary in order to insert stuff into the auto-incremented table.
    @SqlUpdate("create table if not exists last_promised_key " +
               "(id int primary key auto_increment, other_column int default 0)")
    void initializeCountTable();

    /**
     * Generate a new unique key by abusing autoincrement semantics in a separate table.
     * Once "promised" by performing an insert, then it'll either get used with an article
     * or if the write fails, it will never be used, ever.
     */
    @SqlUpdate("insert into last_promised_key (other_column) values (DEFAULT)")
    @GetGeneratedKeys
    int generateKey();

    @SqlUpdate("insert into articles (id, content, parent) values (:id, :content, :parent)")
    void insert(@BindBean Article article);

    @SqlQuery("select * from articles")
    List<Article> getAll();

    @SqlQuery("select * from articles where id = :id")
    Article get(@Bind("id") int id);

    class ArticleMapper implements ResultSetMapper<Article>
    {
        @Override
        public Article map(int i, ResultSet resultSet, StatementContext statementContext)
        throws SQLException
        {
            return new Article(resultSet.getString("content"), resultSet.getInt("parent"))
                    .setId(resultSet.getInt("id"));
        }
    }
}
