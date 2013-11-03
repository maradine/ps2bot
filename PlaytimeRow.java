class PlaytimeRow implements java.io.Serializable {
	private long characterId;
	private int itemId;
	private int lastSave;
	private int value;
	private int vehicleId;
	private int factionId;
	private int brValue;

	public PlaytimeRow() {
		this.characterId=0L;
		this.itemId=0;
		this.lastSave=0;
		this.value=0;
		this.vehicleId=0;
		this.factionId=0;
		this.brValue=0;
	}

	public PlaytimeRow(long s1, int s2, int s3, int s4, int s5, int s6, int s7) {
		this.characterId = s1;
		this.itemId = s2;
		this.lastSave = s3;
		this.value = s4;
		this.vehicleId = s5;
		this.factionId = s6;
		this.brValue = s7;
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
	public int getFactionId() {
		return this.factionId;
	}
	public int getBrValue() {
		return this.brValue;
	}
	
	
	
	public void setValue(int setter) {
		this.value = setter;
	}
}

