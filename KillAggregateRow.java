class KillAggregateRow {
	private int id;
	private int kills;
	private int uniques;
	private float kpu;
	private float avgbr;
	private float q1kpu;
	private float q2kpu;
	private float q3kpu;
	private float q4kpu;
	private String name;
	private int period;

	public KillAggregateRow() {
		this.id=0;
		this.kills=0;
		this.uniques=0;
		this.kpu=0f;
		this.avgbr=0f;
		this.q1kpu=0f;
		this.q2kpu=0f;
		this.q3kpu=0f;
		this.q4kpu=0f;
		this.name="";
		this.period=0;
	}

	public KillAggregateRow(int s1, int s2, int s3, float s4, float s5, float s6, float s7, float s8, float s9, String s10, int s11) {
		this.id = s1;
		this.kills = s2;
		this.uniques = s3;
		this.kpu = s4;
		this.avgbr = s5;
		this.q1kpu = s6;
		this.q2kpu = s7;
		this.q3kpu = s8;
		this.q4kpu = s9;
		this.name = s10;
		this.period = s11;
	}


	public int getId() {
		return this.id;
	}
	public int getKills() {
		return this.kills;
	}
	public int getUniques() {
		return this.uniques;
	}
	public float getKpu() {
		return this.kpu;
	}
	public float getAvgbr() {
		return this.avgbr;
	}
	public float getQ1kpu() {
		return this.q1kpu;
	}
	public float getQ2kpu() {
		return this.q2kpu;
	}
	public float getQ3kpu() {
		return this.q3kpu;
	}
	public float getQ4kpu() {
		return this.q4kpu;
	}
	public String getName() {
		return this.name;
	}
	public int getPeriod() {
		return this.period;
	}
}

