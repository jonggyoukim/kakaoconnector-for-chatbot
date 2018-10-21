package oracle.cloud.bots.kakao.model;

public class Message {
	@Override
	public String toString() {
		return "Message [text=" + text + ", photo=" + photo + ", message_button=" + message_button + "]";
	}

	String text;
	Photo photo;
	MessageButton message_button;

	public Message() {
	}

	public Message(String text, Photo photo, MessageButton message_button) {
		super();
		this.text = text;
		this.photo = photo;
		this.message_button = message_button;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Photo getPhoto() {
		return photo;
	}

	public void setPhoto(Photo photo) {
		this.photo = photo;
	}

	public MessageButton getMessage_button() {
		return message_button;
	}

	public void setMessage_button(MessageButton message_button) {
		this.message_button = message_button;
	}

}