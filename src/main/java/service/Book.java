package service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import service.BooksApi.Message;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetTemporaryLinkErrorException;
import com.dropbox.core.v2.files.WriteMode;
import com.google.api.client.util.Base64;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfNotZero;

/**
 * Represents the Book entity
 */
@Entity
public class Book {
	
	@Id
	private Long id;
	@Index
	private String title;
	private String author;
	private int year;
	@Index
	private int likes;
	private int dislikes;
	private List<String> genre = new ArrayList<String>(0);
	private List<String> quotes = new ArrayList<String>(0);
	private List<String> tags = new ArrayList<String>(0);
	private String annotation;
	private String image;
	private int userlike;
	
	private Book(){}

	/**
	 * Creates the book from BookForm
	 * @param bookForm BookForm
	 */
	public Book(BookForm bookForm) {
		if (bookForm.getTitle()!=null) this.title = bookForm.getTitle();
		else this.title = "";
		if (bookForm.getAuthor()!=null) this.author = bookForm.getAuthor();
		else this.author= "";
		this.year = bookForm.getYear();
		this.likes = 0;
		this.dislikes = 0;
		if (bookForm.getGenre()!=null)this.genre = bookForm.getGenre();
		if (bookForm.getQuotes()!=null)this.quotes = bookForm.getQuotes();
		if (bookForm.getTags()!=null)this.tags = bookForm.getTags();
		if (bookForm.getAnnotation()!=null)this.annotation = bookForm.getAnnotation();
		else this.annotation = "";
		if (bookForm.getImage()!=null) {
			try {
				this.image = saveImage(bookForm.getImage(), title);
			} catch (Exception e) {
				this.image = "";
			}
		} else {
			this.image = "";
		}
	}
	public String getTitle() {
		return title;
	}
	public String getAuthor() {
		return author;
	}
	public int getYear() {
		return year;
	}
	public int getLikes() {
		return likes;
	}
	public int getDislikes() {
		return dislikes;
	}
	public List<String> getGenre() {
		return genre;
	}
	public List<String> getQuotes() {
		return quotes;
	}
	public List<String> getTags() {
		return tags;
	}
	public String getAnnotation() {
		return annotation;
	}
	public long getId() {
		return id;
	}
	public int getLiked () {
		return this.userlike;
	}
	public void setLiked(int liked) {
		this.userlike = liked;
	}
    public String getWebsafeKey() {
        return Key.create(Book.class, id).getString();
    }
	public void like (Boolean bool) {
		likes++;
		if (bool) dislikes--;
	}
	public void dislike (Boolean bool) {
		dislikes++;
		if (bool) likes--;
	}

	/**
	 * @return Url for book cover
	 */
	public String getImage() {
		try {
			return DropboxClient.getClient().files().getTemporaryLink(image).getLink();
		} catch (Exception e) {
			try {
				return DropboxClient.getClient().files().getTemporaryLink("/covers/@DEFAULT/default.jpg").getLink();
			} catch (DbxException e1) {
				return "";
			}
		}
	}

	/**
	 * Saves book cover
	 * @param image Image in Base64
	 * @param title Title of the book
	 * @return Url of the new cover
	 * @throws Exception
	 */
	private static String saveImage(String image, String title) throws Exception {
		String[] split = image.split(";base64,");
		String type = split[0];
		type = type.split("/")[1];
		image = split[1];
		String fileName = "cover."+type;
		byte[] bytes = Base64.decodeBase64(image);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		String url;
		try {
			FileMetadata metadata = DropboxClient.getClient().files().uploadBuilder("/covers/" + title + "/" + fileName)
	                .withMode(WriteMode.ADD)
	                .uploadAndFinish(bis);
			url = metadata.getPathDisplay();
		} finally {
			bis.close();
		}
		
		return url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Book other = (Book) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}