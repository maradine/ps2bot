class PlaytimeRow implements java.io.Serializable {
	private long characterId;
	private int itemId;
	private int lastSave;
	private int value;
	private int vehicleId;

	public PlaytimeRow() {
		this.characterId=0L;
		this.itemId=0;
		this.lastSave=0;
		this.value=0;
		this.vehicleId=0;
	}

	public PlaytimeRow(long s1, int s2, int s3, int s4, int s5) {
		this.characterId = s1;
		this.itemId = s2;
		this.lastSave = s3;
		this.value = s4;
		this.vehicleId = s5;
	}


	public long getCharacterId() {
		return this.characterId;
	}
	public int getItemId() {
		return this.itemId;
	}
	public int getLastSave() {
		return this.lastSave;
	}
	public int getValue() {
		return this.value;
	}
	public int getVehicleId() {
		return this.vehicleId;
	}
	
	public void setValue(int setter) {
		this.value = setter;
	}
}

