class TimePeriod {

	private int id;
	private int start;
	private int end;
	private boolean isDaily;

	public TimePeriod(int id, int start, int end, boolean isDaily) {
		this.id = id;
		this.start = start;
		this.end = end;
		this.isDaily = isDaily;
	}

	public int getId() {
		return id;
	}
	
	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return end;
	}
	
	public boolean getIsDaily() {
		return isDaily;
	}
}
