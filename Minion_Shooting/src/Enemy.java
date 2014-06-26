import java.awt.Point;

public class Enemy {
	// 게임에 등장하는 적 캐릭터 관리 클래스
	Minion_Shooting_frame main;
	Point pos;
	Point _pos;
	Point dis;
	int img;
	int kind;
	int life;
	int mode;
	int cnt;
	int shoottype;
	Bullet bul;

	Enemy(Minion_Shooting_frame main, int img, int x, int y, int kind, int mode) {
		this.main = main;
		pos = new Point(x, y);
		_pos = new Point(x, y);
		dis = new Point(x / 100, y / 100);
		this.kind = kind;
		this.img = img;
		this.mode = mode;
		life = 6 + main.RAND(0, 3) * main.level; // 게임 레벨에 따라 라이프와 탄을 쏘는 시간이 짧아진다.
		System.out.println("life: "+ life);
		cnt = main.RAND(main.level * 5, 80);
		shoottype = main.RAND(0, 2);
	}

	public boolean move() {
		boolean ret = true;

		switch (shoottype) {// 공격 형태에 따라 각기 다른 공격을 한다.
		case 0:// 플레이어를 향해 3발을 점사한다
			if (cnt % 100 == 0 || cnt % 103 == 0 || cnt % 106 == 0) { // cnt로 공격 간격을 체크한다
				bul = new Bullet(pos.x, pos.y, 2, 1, main.getAngle(pos.x, pos.y, main.myx, main.myy), 6);
				main.bullets.add(bul);
			}
			
			break;
		case 1://타이머에 맞춰 2방향탄을 발사한다
			if (cnt % 90 == 0 || cnt % 100 == 0 || cnt % 110 == 0) {
				bul = new Bullet(pos.x, pos.y, 2, 1,
						(0 + (cnt % 36) * 10) % 360, 6);
				main.bullets.add(bul);
			}
			
			break;
		case 2:// 아무런 공격도 하지않는다
			break;
		}

		switch (kind) {
		case 0:
			switch (mode) {
			case 0:
				pos.x -= 500;
				pos.y += 80;
				if (pos.x < main.myx)
					mode = 2;
				break;
			case 1:
				pos.x -= 500;
				pos.y -= 80;
				if (pos.x < main.myx)
					mode = 3;
				break;
			case 2:
				pos.x += 600;
				pos.y += 240;
				break;
			case 3:
				pos.x += 600;
				pos.y -= 240;
				break;
			}
			break;
			
			
		case 1:
			break;
		}
		dis.x = pos.x / 100;
		dis.y = pos.y / 100;
		if (dis.x < 0 || dis.x > 640 || dis.y < 0 || dis.y > 480)
			ret = false;

		cnt++;
		return ret;
	}
}
