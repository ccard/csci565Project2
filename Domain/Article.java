package Domain;

import com.google.common.collect.ComparisonChain;

import java.io.Serializable;

/**
 * @author Chris Card, Steven Ruppert
 * An article to be posted to a {@link BulletinBoard}.
 */
public class Article implements Serializable, Comparable<Article>
{
    /**
     * Article content, full of presumably high quality writing.
     */
    public final String content;

    /**
     * Unique id. Articles are globally ordered, so an article was posted after another article
     * iff its id is higher than the other article.
     *
     * If the article id is `-1`, than its id is UNDEFINED. The server will assign an id when
     * the article is posted.
     */
    public final int id;

    /**
     * Reply parent for this article. a parent of `0` indicates a top-level post, i.e.
     * a reply to the bulletin board itself.
     *
     * Behaviour of articles that reply to nonexistent articles ids is undefined.
     */
    public final int parent;

    /**
     * Construct a new Article with an UNDEFINED id.
     */
    public Article(String body, int parent)
    {
        this(body, -1, parent);
    }

    /**
     * Construct a new Article with a specified id
     */
    private Article(String body, int id, int parent)
    {
        content = body;
        this.id = id;
        this.parent = parent;
    }

    /**
     * A clone of this article with the specified id.
     */
    public Article setId(int id)
    {
        return new Article(this.content, id, this.parent);
    }

    private static final long serialVersionUID = 227L;

    @Override
    public int hashCode()
    {
        return id;
    }

    // getters are necessary for javabeans-based JDBI field detection

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Article article = (Article) o;

        if (id != article.id) return false;

        return true;
    }

    public String getContent()
    {
        return content;
    }

    public int getId()
    {
        return id;
    }

    public int getParent()
    {
        return parent;
    }

    // order by id
    @Override
    public int compareTo(Article that)
    {
        return ComparisonChain.start()
                .compare(this.id, that.id).result();
    }
}
