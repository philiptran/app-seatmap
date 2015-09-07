package cl.seatmap.domain;

/**
 * 
 * @author philiptrannp
 *
 */
public class ContactLocation {
	private int id;
	private String location;
	private int level;
	private int x;
	private int y;
	private String name;
	private int hx;
	private int hy;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(location).append(":(").append(x).append(",").append(y)
				.append(")->").append(name);
		return sb.toString();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getHx() {
		return hx;
	}

	public void setHx(int hx) {
		this.hx = hx;
	}

	public int getHy() {
		return hy;
	}

	public void setHy(int hy) {
		this.hy = hy;
	}
}
