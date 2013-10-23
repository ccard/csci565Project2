/**
* @Author Chris Card
* This contains the interface for the message class
*/

package Compute;

import com.google.common.collect.ComparisonChain;

import java.io.Serializable;
import java.util.*;

public class Article implements Serializable, Comparable<Article>
{
	private static final long serialVersionUID = 227L;

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Article article = (Article) o;

        if (id != article.id) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return id;
    }

    public final String content;
	public final int id;
	public final int parent;

	/**
	* Constructor
	* @param msg message to store
	* @param type the type of action to take when sent to the server, either send or receive
	*/
	public Article(String body, int parent)
	{
		this(body,-1,parent);
	}

	private Article(String body, int id, int parent)
	{
		content = body;
		this.id = id;
		this.parent = parent;
	}

	public Article setId(int id)
	{
		return new Article(this.content,id,this.parent);
	}

    // order by id
    @Override
    public int compareTo(Article that)
    {
        return ComparisonChain.start()
                .compare(this.id, that.id).result();
    }
}
