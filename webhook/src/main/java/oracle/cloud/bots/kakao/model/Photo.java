package oracle.cloud.bots.kakao.model;

public class Photo {
	@Override
	public String toString() {
		return "Photo [url=" + url + ", width=" + width + ", height=" + height + "]";
		// return "Photo [url=" + url + "]";
	}

	String url;
	int width;
	int height;

	public Photo() {
	}

	// public Photo(String url) {
	// super();
	// this.url = url;
	//
	// }
	
	public Photo(String url, int width, int height) {
		super();
		this.url = url;
		this.width = width;
		this.height = height;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url.trim();
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}
