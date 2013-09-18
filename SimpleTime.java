

public class SimpleTime implements Comparable<SimpleTime>{

	private int hours;
	private int minutes;

	public SimpleTime() {
		hours=0;
		minutes=0;
	}

	public SimpleTime(int hours, int minutes) {
		this.hours = hours;
		this.minutes = minutes;
	}

	public SimpleTime(int hours) {
		this.hours = hours;
		this.minutes = 0;
	}

	public int getHours() {
		return this.hours;
	}

	public int getMinutes() {
		return this.minutes;
	}

	public void setHours(int hours) {
		if (hours < 0 || hours > 23) {
			this.hours = 0;
		} else {
			this.hours = hours;
		}
	}

	public void setMinutes(int minutes) {
		if (minutes < 0 || minutes > 59) {
			this.minutes = 0;
		} else {
			this.minutes = minutes;
		}
	}

	public int compareTo(SimpleTime st) {
		if (this.hours < st.getHours()) {
			return -1;
		} else if (this.hours > st.getHours()) {
			return 1;
		} else if (this.minutes < st.getMinutes()) {
			return -1;
		} else if (this.minutes > st.getMinutes()) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public int hashCode() {
		return ((this.hours * 60) + this.minutes);
	}
	
	@Override
	public boolean equals(Object o) {
		SimpleTime st = (SimpleTime) o;
		if (this.hours == st.getHours() && this.minutes == st.getMinutes()) {
			return true;
		} else {
			return false;
		}
	}


}
