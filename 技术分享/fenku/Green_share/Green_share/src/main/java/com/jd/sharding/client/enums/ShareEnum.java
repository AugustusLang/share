package com.jd.sharding.client.enums;

public final class ShareEnum {
	public static enum TRANTYPE {
		SINGLE("single", 1), ATOMIK("atomik", 2);
		private String name;
		private int value;

		private TRANTYPE(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public int getValue() {
			return value;
		}

		public static TRANTYPE fromValue(int value) {
			for (TRANTYPE ce : TRANTYPE.values()) {
				if (ce.value == value)
					return ce;
			}
			return null;
		}

		public static TRANTYPE fromName(String name) {
			if (name == null || name.length() == 0)
				return null;
			for (TRANTYPE ce : TRANTYPE.values()) {
				if (ce.name.equals(name))
					return ce;
			}
			return null;
		}
	}

}
