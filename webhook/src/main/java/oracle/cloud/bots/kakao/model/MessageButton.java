package oracle.cloud.bots.kakao.model;

public class MessageButton {
	@Override
	public String toString() {
		return "MessageButton [label=" + label + ", url=" + url + "]";
	}

	String label;
	String url;

	public MessageButton() {
	};

	public MessageButton(String label, String url) {
		super();
		this.label = label;
		this.url = url;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}