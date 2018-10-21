package oracle.cloud.bots.kakao.model;

import java.util.List;

public class Keyboard {
		String type;
		List<String> buttons;

		public Keyboard(){}
		
		public Keyboard(String type, List<String> buttons) {
			super();
			this.type = type;
			this.buttons = buttons;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public List<String> getButtons() {
			return buttons;
		}

		@Override
		public String toString() {
			return "Keyboard [type=" + type + ", buttons=" + buttons + "]";
		}

		public void setButtons(List<String> button) {
			this.buttons = button;
		}

	}