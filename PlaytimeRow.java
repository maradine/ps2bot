class PlaytimeRow {
	private long characterId;
	private int itemId;
	private long lastSave;
	private int value;

	public PlaytimeRow() {
		this.characterId=0L;
		this.itemId=0;
		this.lastSave=0L;
		this.value=0;
	}

	public PlaytimeRow(long s1, int s2, long s3, int s4) {
		this.characterId = s1;
		this.itemId = s2;
		this.lastSave = s3;
		this.value = s4;
	}


	public long getCharacterId() {
		return this.characterId;
	}
	public int getItemId() {
		return this.itemId;
	}
	public long getLastSave() {
		return this.lastSave;
	}
	public int getValue() {
		return this.value;
	}
}

