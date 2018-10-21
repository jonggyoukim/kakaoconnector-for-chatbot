package oracle.cloud.bots.kakao.model;

import java.util.List;

public class KAKAO {
	Message message;
	Keyboard keyboard;
	boolean chunked;

	public boolean isChunked() {
		return chunked;
	}

	public void setChunked(boolean chunked) {
		this.chunked = chunked;
	}

	public KAKAO() {
	}

	public KAKAO(String msg) {
		if (msg != null) {
			this.message = new Message();
			this.message.setText(msg);
		}
	}

	public KAKAO(Message message, Keyboard keyboard) {
		super();
		this.message = message;
		this.keyboard = keyboard;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Keyboard getKeyboard() {
		return keyboard;
	}

	public void setKeyboard(Keyboard keyboard) {
		this.keyboard = keyboard;
	}

	public void merge(KAKAO k) {
		if (k != null) {
			if (k.getMessage() != null) {
				if (this.getMessage() != null) {
					if (k.getMessage().getText() != null) {
						String text = this.getMessage().getText();
						text = text.concat("\n" + k.getMessage().getText());
						this.getMessage().setText(text);
					}
				} else {
					if (k.getMessage().getText() != null) {
						this.setMessage(new Message());
						this.getMessage().setText(k.getMessage().getText());
					}
				}
			}
			if (k.getKeyboard() != null) {
				if (this.getKeyboard() != null) {
					if (k.getKeyboard().getButtons() != null) {
						List<String> buttons = this.getKeyboard().getButtons();
						buttons.addAll(k.getKeyboard().getButtons());
						this.getKeyboard().setButtons(buttons);
					}
				} else {
					if (k.getKeyboard().getButtons() != null) {
						this.setKeyboard(new Keyboard());
						this.getKeyboard().setButtons(k.getKeyboard().getButtons());
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return "KAKAO [message=" + message + ", keyboard=" + keyboard + ", chunked=" + chunked + "]";
	}

}
