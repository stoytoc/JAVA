import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

public class GameScreen extends Canvas {
	// 실제 게임의 메인 제어를 행하는 클래스
	// 가급적 화면 출력에서는 게임에서 공통으로 쓰이는 변수값의 변경 등을 행하지 않는 것이 좋다.
	Minion_Shooting_frame main;
	Minion_Shooting_frame main2;
	int cnt, gamecnt;
	int cnt2, gamecnt2;

	// 화면 그리기용 변수
	int x, y;// 플레이어 캐릭터의 좌표

	Image dblbuff;// 더블버퍼링용 백버퍼
	Graphics gc;// 더블버퍼링용 그래픽 컨텍스트

	Image dblbuff2;// 더블버퍼링용 백버퍼
	Graphics gc2;// 더블버퍼링용 그래픽 컨텍스트

	Image bg1, bg2, bg3, bg_f;// 배경화면
	Image cloud[] = new Image[5];// 구름
	Image star[] = new Image[5]; // 별별
	Image title, title_key;// 타이틀화면

	Image chr[] = new Image[9];// 캡틴 아메리카 미니언
	Image item_chr[] = new Image[9]; // 슈퍼맨 미니언

	Image enemy[] = new Image[5];// 적 캐릭터
	Image bullet[] = new Image[4]; // 총알
	Image explo;// 폭염
	Image item[] = new Image[4];// 아이템

	Image _start;// 시작로고
	Image _over;// 게임오버로고
	Image shield;// 실드

	Font font;

	int v[] = { -2, -1, 0, 1, 2, 1, 0, -1 };
	int v2[] = { -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 7,
			6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7 };
	int step = 0;

	GameScreen(Minion_Shooting_frame main) {
		this.main = main;
		this.main2 = main;
		font = new Font("Default", Font.PLAIN, 9);
	}

	public void paint(Graphics g) {
		// 더블 버퍼링용 오프스크린 버퍼 생성. 필히 paint 함수 내에서 해 줘야 한다. 그렇지 않으면 null이 반환된다.
		if (gc == null) {
			dblbuff = createImage(main.gScreenWidth, main.gScreenHeight);
			if (dblbuff == null || dblbuff == null)
				System.out.println("오프스크린 버퍼 생성 실패");
			else {
				gc = dblbuff.getGraphics(); // 오프스크린 버퍼에 그리기 위한 그래픽 컨텍스트 획득
			}
			return;
		}

		if (gc2 == null) {
			dblbuff2 = createImage(main.gScreenWidth, main.gScreenHeight);
			if (dblbuff2 == null || dblbuff2 == null)
				System.out.println("오프스크린 버퍼 생성 실패");
			else {
				gc2 = dblbuff.getGraphics();
			}
			return;
		}
		update(g);
	}

	public void update(Graphics g) {
		// 화면 깜박거림을 줄이기 위해, paint에서 화면을 바로 묘화하지 않고 update 메소드를 호출하게 한다.
		cnt = main.cnt;
		gamecnt = main.gamecnt;

		cnt2 = main2.cnt;
		gamecnt2 = main2.gamecnt;

		if (gc == null)
			return;
		dblpaint();// 오프스크린 버퍼에 그리기
		g.drawImage(dblbuff, 0, 0, this);// 오프스크린 버퍼를 메인화면에 그린다.
		g.drawImage(dblbuff2, 0, 0, this);
	}

	public void dblpaint() {
		// 실제 그리는 동작은 이 함수에서 모두 행한다.
		switch (main.status) {
		case 0:// 타이틀화면
			Draw_TITLE();
			gc.setColor(new Color(0));
			break;
		case 1:// 게임 스타트
			Draw_BG();
			Draw_MY();
			Draw_BG2();
			drawImageAnc(_start, 0, 270, 3);
			break;
		case 2:// 게임화면
		case 4:// 일시정지
			Draw_BG();
			Draw_MY();
			Draw_ENEMY();
			Draw_BULLET();
			Draw_EFFECT();
			Draw_ITEM();
			Draw_BG2();
			Draw_UI();
			break;
		case 3:// 게임오버
			Draw_BG();
			Draw_ENEMY();
			Draw_BULLET();
			Draw_BG2();
			Draw_UI();
			drawImageAnc(_over, 320, 240, 4);
			break;
		default:
			break;
		}
	}

	public void Draw_TITLE() {
		gc.drawImage(title, 0, 0, this);
		if (cnt % 20 < 10)
			gc.drawImage(title_key, 320 - 201, 370, this);
	}

	public void Draw_BG() {
		int i;
		if (main.level == 0) {

			gc.drawImage(bg1, 0, 0, this);
			for (i = 0; i < 12; i++)
				gc.drawImage(cloud[3], i * 64 - ((cnt / 2) % 128), 370, this);
			for (i = -1; i < 14; i++)
				gc.drawImage(cloud[2], i * 64 - (cnt % 128) * 2, 395, this);
		}
		if (main.level == 1) {
			gc.drawImage(bg2, 0, 0, this);
			for (i = 0; i < 12; i++)
				gc.drawImage(star[3], i * 64 - ((cnt / 2) % 128), 370, this);
			for (i = -1; i < 14; i++)
				gc.drawImage(star[2], i * 64 - (cnt % 128) * 2, 395, this);
		}
		if (main.level == 2) {
			gc.drawImage(bg3, 0, 0, this);
			for (i = 0; i < 12; i++)
				gc.drawImage(star[3], i * 64 - ((cnt / 2) % 128), 370, this);
			for (i = -1; i < 14; i++)
				gc.drawImage(star[2], i * 64 - (cnt % 128) * 2, 395, this);
		}
	}

	public void Draw_BG2() {
		int i;

		step = (cnt % (bg_f.getWidth(this) / main.scrspeed)) * main.scrspeed;
		gc.drawImage(bg_f, 0 - step, 540 - bg_f.getHeight(this)
				+ v[(cnt / 20) % 8] * 2, this);
		if (main.level == 0) {
			if (step >= bg_f.getWidth(this) - main.gScreenWidth) {
				gc.drawImage(bg_f, 0 - step + bg_f.getWidth(this),
						540 - bg_f.getHeight(this) + v[(cnt / 20) % 8] * 2,
						this);
			}
			for (i = -1; i < 14; i++)
				gc.drawImage(cloud[0], i * 128 - (cnt % 128) * 8, 435, this);
		}
		if (main.level == 1) {
			if (step >= bg_f.getWidth(this) - main.gScreenWidth) {
				gc.drawImage(bg_f, 0 - step + bg_f.getWidth(this),
						540 - bg_f.getHeight(this) + v[(cnt / 20) % 8] * 2,
						this);
			}
			for (i = -1; i < 14; i++)
				gc.drawImage(star[0], i * 128 - (cnt % 128) * 8, 435, this);
		}
		if (main.level == 2) {
			if (step >= bg_f.getWidth(this) - main.gScreenWidth) {
				gc.drawImage(bg_f, 0 - step + bg_f.getWidth(this),
						540 - bg_f.getHeight(this) + v[(cnt / 20) % 8] * 2,
						this);
			}
			for (i = -1; i < 14; i++)
				gc.drawImage(star[0], i * 128 - (cnt % 128) * 8, 435, this);
		}
	}

	public void Draw_MY() {
		int myx, myy;
		myx = main.myx / 100;
		myy = main.myy / 100;
		if (main.myshield > 2)
			drawImageAnc(
					shield,
					(int) (Math.sin(Math.toRadians((cnt % 72) * 5)) * 16 + myx),
					(int) (Math.cos(Math.toRadians((cnt % 72) * 5)) * 16 + myy),
					(main.cnt / 6 % 7) * 64, 0, 64, 64, 4);// 실드 라이프가 3 이상
		else if (main.myshield > 0 && main.cnt % 4 < 2)
			drawImageAnc(
					shield,
					(int) (Math.sin(Math.toRadians((cnt % 72) * 5)) * 16 + myx),
					(int) (Math.cos(Math.toRadians((cnt % 72) * 5)) * 16 + myy),
					(main.cnt / 6 % 7) * 64, 0, 64, 64, 4);// 실드 라이프가 1, 2면 점멸

		switch (main.mymode) {
		case 0:// 무적
		case 1:// 무적이면서 등장
			if (main.cnt % 20 < 10)
				drawImageAnc(chr[2 + (main.cnt / 5) % 2], myx, myy, 4);
			break;
		case 2:// 온플레이
			if (main.minionItem) {
				if (main.myimg == 6) {
					drawImageAnc(chr[main.myimg + (main.cnt / 3) % 2], myx,
							myy, 4);
					drawImageAnc(
							item_chr[main.itemimg + (main.itemcnt / 3) % 2],
							myx + 50, myy, 4);
				} else if (main.myimg != 8) {
					drawImageAnc(chr[main.myimg + (main.cnt / 5) % 2], myx,
							myy, 4);
					drawImageAnc(
							item_chr[main.itemimg + (main.itemcnt / 5) % 2],
							myx + 50, myy, 4);
				} else if (main.myimg == 8) {
					drawImageAnc(chr[main.myimg], myx, myy, 4);
					drawImageAnc(item_chr[main.itemimg], myx + 50, myy, 4);
				}

			} else {
				if (main.myimg == 6)
					drawImageAnc(chr[main.myimg + (main.cnt / 3) % 2], myx,
							myy, 4);
				else if (main.myimg != 8)
					drawImageAnc(chr[main.myimg + (main.cnt / 5) % 2], myx,
							myy, 4);
				else if (main.myimg == 8)
					drawImageAnc(chr[main.myimg], myx, myy, 4);

			}
			break;
		case 3:// 데미지
			if (main.cnt % 6 < 3)
				drawImageAnc(chr[8], myx, myy, 4);
			break;
		}
	}

	public void Draw_ENEMY() {
		int i;
		Enemy buff;
		for (i = 0; i < main.enemies.size(); i++) {
			buff = (Enemy) (main.enemies.elementAt(i));
			drawImageAnc(enemy[0], buff.dis.x, buff.dis.y,
					((cnt / 8) % 7) * 36, 0, 36, 36, 4);
		}
	}

	public void Draw_BULLET() {
		Bullet buff;
		int i;
		for (i = 0; i < main.bullets.size(); i++) {
			buff = (Bullet) (main.bullets.elementAt(i));
			switch (buff.img_num) {
			case 0:
			case 1:
			case 2:
			case 3:
				drawImageAnc(bullet[buff.img_num], buff.dis.x - 6,
						buff.dis.y - 6, 4);
				break;
			}
		}
	}

	public void Draw_EFFECT() {
		int i;
		Effect buff;
		for (i = 0; i < main.effects.size(); i++) {
			buff = (Effect) (main.effects.elementAt(i));
			drawImageAnc(explo, buff.dis.x, buff.dis.y,
					((16 - buff.cnt) % 4) * 64, ((16 - buff.cnt) / 4) * 64, 64,
					64, 4);
		}
	}

	public void Draw_ITEM() {
		int i;
		Item buff;
		for (i = 0; i < main.items.size(); i++) {
			buff = (Item) (main.items.elementAt(i));
			drawImageAnc(item[buff.kind], buff.dis.x, buff.dis.y,
					((main.cnt / 4) % 7) * 36, 0, 36, 36, 4);
		}
	}

	public void Draw_UI() {
		String str1 = "" + main.score;
		String str2 = "" + main.mylife;
		String str3 = "" + main.myspeed;
		String str4 = "" + (main.level + 1);
		String str5 = "[1] Speed down   [2] Speed up   [3] Pause";
		gc.setFont(new Font("Default", Font.BOLD, 24));
		gc.setColor(new Color(0xf7d84b));
		gc.drawString("SCORE: ", 9, 50);
		gc.drawString(str1, 100, 50);
		gc.drawString("LIFE:", 150, 50);
		gc.drawString(str2, 210, 50);
		gc.drawString("SPEED:", 235, 50);
		gc.drawString(str3, 325, 50);
		gc.drawString("LEVEL:", 345, 50);
		gc.drawString(str4, 435, 50);
		gc.setFont(new Font("Default", Font.PLAIN, 20));
		//gc.setColor(new Color(0x0));
		gc.drawString(str5, 9, main.gScreenHeight - 10);
	}

	public void drawImageAnc(Image img, int x, int y, int anc) {
		// 앵커값을 참조해 이미지 출력 위치를 보정한다.
		// 예) anc==0 : 좌상단이 기준, anc==4 : 이미지 중앙이 기준
		int imgw, imgh;
		imgw = img.getWidth(this);
		imgh = img.getHeight(this);
		x = x - (anc % 3) * (imgw / 2);
		y = y - (anc / 3) * (imgh / 2);

		gc.drawImage(img, x, y, this);
	}

	public void drawImageAnc(Image img, int x, int y, int sx, int sy, int wd,
			int ht, int anc) {
		// sx, sy부터 wd, ht만큼 클리핑해서 그린다.
		if (x < 0) {
			wd += x;
			sx -= x;
			x = 0;
		}

		if (y < 0) {
			ht += y;
			sy -= y;
			y = 0;
		}

		if (wd < 0 || ht < 0)
			return;
		x = x - (anc % 3) * (wd / 2);
		y = y - (anc / 3) * (ht / 2);
		gc.setClip(x, y, wd, ht);
		gc.drawImage(img, x - sx, y - sy, this);
		gc.setClip(0, 0, main.gScreenWidth + 10, main.gScreenHeight + 30);
	}

}
