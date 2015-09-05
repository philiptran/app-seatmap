package cl.seatmap.widget;

/**
 * 
 * @author philiptrannp
 *
 */
public class OverlayItem {
	protected String title;
	protected String snippet;
	protected String imageURL;

	public OverlayItem() {
		this("Title", "description", null);
	}

	public OverlayItem(String title, String snippet) {
		this(title, snippet, null);
	}

	public OverlayItem(String title, String snippet, String imageURL) {
		super();
		this.title = title;
		this.snippet = snippet;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

}
