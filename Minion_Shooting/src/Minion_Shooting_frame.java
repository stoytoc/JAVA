import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.Random;
import java.util.Vector;

public class Minion_Shooting_frame extends Frame implements KeyListener, Runnable {
	public final static int UP_PRESSED = 0x001;
	public final static int DOWN_PRESSED = 0x002;
	public final static int LEFT_PRESSED = 0x004;
	public final static int RIGHT_PRESSED = 0x008;
	public final static int FIRE_PRESSED = 0x010;

	GameScreen gamescreen;// Canvas 객체를 상속한 화면 묘화 메인 클래스

	Thread mainwork;// 스레드 객체
	boolean roof = true;// 스레드 루프 정보
	Random rnd = new Random(); // 랜덤 선언

	// 게임 제어를 위한 변수
	int status;// 게임의 상태
	int cnt;// 루프 제어용 컨트롤 변수
	int delay;// 루프 딜레이. 1/1000초 단위.
	long pretime;// 루프 간격을 조절하기 위한 시간 체크값
	int keybuff;// 키 버퍼값

	// 게임용 변수
	int score;// 점수
	int mylife;// 남은 목숨
	int gamecnt;// 게임 흐름 컨트롤
	int scrspeed = 16;// 스크롤 속도
	int level;// 게임 레벨

	int myx, myy;// 플레이어 위치. 화면 좌표계에 *100 된 상태.
	int myspeed;// 플레이어 이동 속도
	int mydegree;// 플레이어 이동 방향
	int mywidth, myheight;// 플레이어 캐릭터의 너비 높이
	int mymode = 1;// 플레이어 캐릭터의 상태 (0부터 순서대로 무적,등장(무적),온플레이,데미지,사망)
	int myimg;// 플레이어 이미지
	int mycnt;
	
	//******************************************************************
	int itemx, itemy;// 플레이어 위치. 화면 좌표계에 *100 된 상태.
	int itemspeed;// 플레이어 이동 속도
	int itemdegree;// 플레이어 이동 방향
	int itemwidth, itemheight;// 플레이어 캐릭터의 너비 높이
	int itemmode = 1;// 플레이어 캐릭터의 상태 (0부터 순서대로 무적,등장(무적),온플레이,데미지,사망)
	int itemimg;// 플레이어 이미지
	int itemcnt;
	boolean itemshoot=false;
	int myMinion;
	
	boolean myshoot = false; // 총알 발사가 눌리고 있는가
	int myshield; // 실드 남은 수비량

	int gScreenWidth = 640;// 게임 화면 너비
	int gScreenHeight = 480;// 게임 화면 높이

	Vector bullets = new Vector();// 총알 관리. 총알의 갯수를 예상할 수 없기 때문에 가변적으로 관리한다.
	Vector enemies = new Vector();// 적 캐릭터 관리.
	Vector effects = new Vector();// 이펙트 관리
	Vector items = new Vector();// 아이템 관리
	// 가변 테이블을 사용한 관리는 처리속도에 악영향을 끼칠 수 있다.
	
	// ****************************************************************************************
	boolean bananaItem = false;
	boolean minionItem = false;
	
	URL MinionSound = this.getClass().getResource("Minion.wav");
	URL bananaSound = this.getClass().getResource("Banana.wav");
	URL coinSound = this.getClass().getResource("dingdong.wav");
	URL shieldSound = this.getClass().getResource("shield.wav");
	URL ItemMiSound = this.getClass().getResource("ItemMi.wav");

	AudioClip snd = Applet.newAudioClip(MinionSound);
	AudioClip sb = Applet.newAudioClip(bananaSound);
	AudioClip sc = Applet.newAudioClip(coinSound);
	AudioClip ss = Applet.newAudioClip(shieldSound);
	AudioClip si = Applet.newAudioClip(ItemMiSound);

	// ****************************************************************************************

	Minion_Shooting_frame() {
		gamescreen = new GameScreen(this);
		gamescreen.setBounds(0, 0, gScreenWidth, gScreenHeight);
		add(gamescreen);

		systeminit();
		initialize();

		// 기본적인 윈도우 정보 세팅. 게임과 직접적인 상관은 없이 게임 실행을 위한 창을 준비하는 과정.
		setIconImage(makeImage("./rsc/icon.png"));
		setBackground(new Color(0xffffff)); // 윈도우 기본 배경색 지정
		setTitle("FLY MINION!_!");// 윈도우 이름 지정
		setLayout(null);// 윈도우의 레이아웃을 프리로 설정
		setBounds(100, 100, 640, 480);// 윈도우의 시작 위치와 너비 높이 지정
		setResizable(false);// 윈도우의 크기를 변경할 수 없음
		setVisible(true);

		addKeyListener(this);// 키 입력 이벤트 리스너 활성화
		addWindowListener(new MyWindowAdapter());// 윈도우의 닫기 버튼 활성화

	}

	public void systeminit() {// 프로그램 초기화
		status = 0;
		cnt = 0;
		delay = 17; //17/1000초 = 58 (프레임/초)
		keybuff = 0;

		mainwork = new Thread(this);
		mainwork.start();
	}

	public void initialize() {// 게임 초기화
		Init_TITLE();
		gamescreen.repaint();
	}

	// 스레드 파트
	public void run() {
		try {
			while (roof) {			
				pretime = System.currentTimeMillis();
				gamescreen.repaint();// 화면 리페인트
				process();// 각종 처리
				keyprocess();// 키 처리

				if (System.currentTimeMillis() - pretime < delay)
					Thread.sleep(delay - System.currentTimeMillis() + pretime);
				// 게임 루프를 처리하는데 걸린 시간을 체크해서 딜레이값에서 차감하여 딜레이를 일정하게 유지한다.
				// 루프 실행 시간이 딜레이 시간보다 크다면 게임 속도가 느려지게 된다.

				if (status != 4)
					cnt++;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 키 이벤트 리스너 처리
	public void keyPressed(KeyEvent e) {
		// if(status==2&&(mymode==2||mymode==0)){
		if (status == 2) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				keybuff |= FIRE_PRESSED;
				break;
			case KeyEvent.VK_LEFT:
				keybuff |= LEFT_PRESSED;// 멀티키의 누르기 처리
				break;
			case KeyEvent.VK_UP:
				keybuff |= UP_PRESSED;
				break;
			case KeyEvent.VK_RIGHT:
				keybuff |= RIGHT_PRESSED;
				break;
			case KeyEvent.VK_DOWN:
				keybuff |= DOWN_PRESSED;
				break;
			case KeyEvent.VK_1:
				if (myspeed > 1)
					myspeed--;
				break;
			case KeyEvent.VK_2:
				if (myspeed < 9)
					myspeed++;
				break;
			case KeyEvent.VK_3:
				if (status == 2)
					status = 4;
				break;
			/*
			 * case KeyEvent.VK_1: System.out.println("이펙트 테스트"); Effect
			 * effect=new Effect(0,
			 * RAND(30,gScreenWidth-30)*100,RAND(30,gScreenHeight-30)*100, 0);
			 * effects.add(effect); break;
			 */
			default:
				break;
			}
		}

		else if (status != 2)
			keybuff = e.getKeyCode();
	}

	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE:
			keybuff &= ~FIRE_PRESSED;
			myshoot = true;
			break;
		case KeyEvent.VK_LEFT:
			keybuff &= ~LEFT_PRESSED;
			break;
		case KeyEvent.VK_UP:
			keybuff &= ~UP_PRESSED;
			break;
		case KeyEvent.VK_RIGHT:
			keybuff &= ~RIGHT_PRESSED;
			break;
		case KeyEvent.VK_DOWN:
			keybuff &= ~DOWN_PRESSED;
			break;
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	// 각종 판단, 변수나 이벤트, CPU 관련 처리
	private void process() {
		switch (status) {
		case 0:// 타이틀화면
			break;
		case 1:// 스타트
			process_MY();
			if (mymode == 2)
				status = 2;
			break;
		case 2:// 게임화면
			process_MY();
			process_ENEMY();
			process_BULLET();
			process_EFFECT();
			process_GAMEFLOW();
			process_ITEM();
			break;
		case 3:// 게임오버
			process_ENEMY();
			process_BULLET();
			process_GAMEFLOW();
			break;
		case 4:// 일시정지
			snd.stop();
			break;
		default:
			break;
		}
		if (status != 4)
			gamecnt++;
	}

	// 키 입력 처리
	// 키 이벤트에서 입력 처리를 할 경우, 이벤트 병목현상이 발생할 수 있으므로
	// 이벤트에서는 키 버퍼만을 변경하고, 루프 내에서 버퍼값에 따른 처리를 한다.
	private void keyprocess() {
		switch (status) {
		case 0: // 타이틀화면
			if (keybuff == KeyEvent.VK_SPACE) {
				Init_GAME();
				Init_MY();
				status = 1;
			}
			break;
		case 2: // 게임화면
			if (mymode == 2 || mymode == 0) {
				if(minionItem) {
					switch (keybuff) {
					case 0:
						mydegree = -1;
						myimg = 0;
						itemdegree = -1;
						itemimg = 0;
						break;
					case FIRE_PRESSED:
						mydegree = -1;
						myimg = 6;
						itemdegree = -1;
						itemimg = 6;
						break;
					case UP_PRESSED:
						mydegree = 0;
						myimg = 2;
						itemdegree = 0;
						itemimg = 2;
						break;
					case UP_PRESSED | FIRE_PRESSED:
						mydegree = 0;
						myimg = 6;
						itemdegree = 0;
						itemimg = 6;
						break;
					case LEFT_PRESSED:
						mydegree = 90;
						myimg = 4;
						itemdegree = 90;
						itemimg = 4;
						break;
					case LEFT_PRESSED | FIRE_PRESSED:
						mydegree = 90;
						myimg = 6;
						itemdegree = 90;
						itemimg = 6;
						break;
					case RIGHT_PRESSED:
						mydegree = 270;
						myimg = 2;
						itemdegree = 270;
						itemimg = 2;
						break;
					case RIGHT_PRESSED | FIRE_PRESSED:
						mydegree = 270;
						myimg = 6;
						itemdegree = 270;
						itemimg = 6;
						break;
					case UP_PRESSED | LEFT_PRESSED:
						mydegree = 45;
						myimg = 4;
						itemdegree = 45;
						itemimg = 4;
						break;
					case UP_PRESSED | LEFT_PRESSED | FIRE_PRESSED:
						mydegree = 45;
						myimg = 6;
						itemdegree = 45;
						itemimg = 6;
						break;
					case UP_PRESSED | RIGHT_PRESSED:
						mydegree = 315;
						myimg = 2;
						itemdegree = 315;
						itemimg = 2;
						break;
					case UP_PRESSED | RIGHT_PRESSED | FIRE_PRESSED:
						mydegree = 315;
						myimg = 6;
						itemdegree = 315;
						itemimg = 6;
						break;
					case DOWN_PRESSED:
						mydegree = 180;
						myimg = 2;
						itemdegree = 180;
						itemimg = 2;
						break;
					case DOWN_PRESSED | FIRE_PRESSED:
						mydegree = 180;
						myimg = 6;
						itemdegree = 180;
						itemimg = 6;
						break;
					case DOWN_PRESSED | LEFT_PRESSED:
						mydegree = 135;
						myimg = 4;
						itemdegree = 135;
						itemimg = 4;
						break;
					case DOWN_PRESSED | LEFT_PRESSED | FIRE_PRESSED:
						mydegree = 135;
						myimg = 6;
						itemdegree = 135;
						itemimg = 6;
						break;
					case DOWN_PRESSED | RIGHT_PRESSED:
						mydegree = 225;
						myimg = 2;
						itemdegree = 225;
						itemimg = 2;
						break;
					case DOWN_PRESSED | RIGHT_PRESSED | FIRE_PRESSED:
						mydegree = 225;
						myimg = 6;
						itemdegree = 225;
						itemimg = 6;
						break;
					default:
						keybuff = 0;
						mydegree = -1;
						myimg = 0;
						itemdegree = -1;
						itemimg = 0;
						break;
					}
				}
				
				else {
					switch (keybuff) {
					case 0:
						mydegree = -1;
						myimg = 0;
						break;
					case FIRE_PRESSED:
						mydegree = -1;
						myimg = 6;
						break;
					case UP_PRESSED:
						mydegree = 0;
						myimg = 2;
						break;
					case UP_PRESSED | FIRE_PRESSED:
						mydegree = 0;
						myimg = 6;
						break;
					case LEFT_PRESSED:
						mydegree = 90;
						myimg = 4;
						break;
					case LEFT_PRESSED | FIRE_PRESSED:
						mydegree = 90;
						myimg = 6;
						break;
					case RIGHT_PRESSED:
						mydegree = 270;
						myimg = 2;
						break;
					case RIGHT_PRESSED | FIRE_PRESSED:
						mydegree = 270;
						myimg = 6;
						break;
					case UP_PRESSED | LEFT_PRESSED:
						mydegree = 45;
						myimg = 4;
						break;
					case UP_PRESSED | LEFT_PRESSED | FIRE_PRESSED:
						mydegree = 45;
						myimg = 6;
						break;
					case UP_PRESSED | RIGHT_PRESSED:
						mydegree = 315;
						myimg = 2;
						break;
					case UP_PRESSED | RIGHT_PRESSED | FIRE_PRESSED:
						mydegree = 315;
						myimg = 6;
						break;
					case DOWN_PRESSED:
						mydegree = 180;
						myimg = 2;
						break;
					case DOWN_PRESSED | FIRE_PRESSED:
						mydegree = 180;
						myimg = 6;
						break;
					case DOWN_PRESSED | LEFT_PRESSED:
						mydegree = 135;
						myimg = 4;
						break;
					case DOWN_PRESSED | LEFT_PRESSED | FIRE_PRESSED:
						mydegree = 135;
						myimg = 6;
						break;
					case DOWN_PRESSED | RIGHT_PRESSED:
						mydegree = 225;
						myimg = 2;
						break;
					case DOWN_PRESSED | RIGHT_PRESSED | FIRE_PRESSED:
						mydegree = 225;
						myimg = 6;
						break;
					default:
						keybuff = 0;
						mydegree = -1;
						myimg = 0;
						break;
					}
				}
			}
			break;
		case 3: //게임 오버
			if (gamecnt++ >= 200 && keybuff == KeyEvent.VK_SPACE) {
				Init_TITLE();
				status = 0;
				keybuff = 0;
			}
			break;
		case 4: //일시정지 해제
			if (gamecnt++ >= 200 && keybuff == KeyEvent.VK_3)
				status = 2;
			snd.play();
			break;
		default:
			break;
		}
	}

	// 서브루틴 일람
	public void Init_TITLE() {
		gamescreen.title = makeImage("./rsc/title.png");
		gamescreen.title_key = makeImage("./rsc/pushspace3.png");
		snd.stop();
	}

	public void Init_GAME() {
		int i;
		gamescreen.bg1 = makeImage("./rsc/cloud1.jpg "); //level 1
		gamescreen.bg2 = makeImage("./rsc/space.png");  //level 2
		gamescreen.bg3 = makeImage("./rsc/moon.png");   //level 3
	
		gamescreen.bg_f = makeImage("./rsc/bg_f.png");
		for (i = 0; i < 1; i++)
			gamescreen.cloud[i] = makeImage("./rsc/cloud" + i + ".png");
		for (i = 0; i < 1; i++)
			gamescreen.star[i] = makeImage("./rsc/star" + i + ".png");
		for (i = 0; i < 4; i++)
			gamescreen.bullet[i] = makeImage("./rsc/game/bullet_" + i + ".png");
		
		gamescreen.enemy[0] = makeImage("./rsc/game/enemy1.png");
		gamescreen.explo = makeImage("./rsc/game/explode.png");
		gamescreen.item[0] = makeImage("./rsc/game/itempoint.png");
		gamescreen.item[1] = makeImage("./rsc/game/item1.png");
		gamescreen.item[2] = makeImage("./rsc/game/item2.png");
		gamescreen.item[3] = makeImage("./rsc/game/minion2.png");

		gamescreen._start = makeImage("./rsc/game/start1.png");
		gamescreen._over = makeImage("./rsc/game/gameover1.png");
		gamescreen.shield = makeImage("./rsc/game/shield_0.png");
		keybuff = 0;
		bullets.clear();
		enemies.clear();
		effects.clear();
		items.clear();
		level = 0;
		
		snd.play();
	}

	public void Init_MY() {
		for (int i = 0; i < 9; i++) {
			if (i < 10)
				gamescreen.chr[i] = makeImage("./rsc/player/my_0" + i + ".png");
			else
				gamescreen.chr[i] = makeImage("./rsc/player/my_" + i + ".png");
		}
		Init_MYDATA();
	}
	
	public void Init_item() {
			for (int i = 0; i < 9; i++) {
				if (i < 10) {
					gamescreen.chr[i] = makeImage("./rsc/player/my_0" + i + ".png");
					gamescreen.item_chr[i] = makeImage("./rsc/player/my2_0" + i + ".png");
				}
				else {
					gamescreen.chr[i] = makeImage("./rsc/player/my_" + i + ".png");
					gamescreen.item_chr[i] = makeImage("./rsc/player/my2_" + i + ".png");
				}
			}
			
			itemx = myx+20;
			itemy = myy-20;
			itemspeed = 4; 
			itemdegree = -1; 
			itemmode = 2; 
			itemimg = 2;
			itemcnt = 0;
	}

	public void Init_MYDATA() {
		score = 0;
		myx = 0;
		myy = 23000;
		myspeed = 4;
		mydegree = -1;
		// mywidth, myheight; //플레이어 캐릭터의 너비 높이
		mymode = 1;
		myimg = 2;
		mycnt = 0;
		mylife = 5;
		keybuff = 0;
	}


	public void process_MY() {
		Bullet shoot;
		switch (mymode) {
		case 1: // 등장
			myx += 200;
			if (myx > 20000)
				mymode = 2;
			break;
		case 0: // 무적
			if (mycnt-- == 0) {
				mymode = 2;
				myimg = 0;
			}
		case 2: // 온플레이
			if (mydegree > -1) {
				if(minionItem) {
					myx -= (myspeed * Math.sin(Math.toRadians(mydegree)) * 100);
					myy -= (myspeed * Math.cos(Math.toRadians(mydegree)) * 100);
					itemx -= (itemspeed * Math.sin(Math.toRadians(itemdegree)) * 100);
					itemy -= (itemspeed * Math.cos(Math.toRadians(itemdegree)) * 100);
					
				}
				else {
					myx -= (myspeed * Math.sin(Math.toRadians(mydegree)) * 100);
					myy -= (myspeed * Math.cos(Math.toRadians(mydegree)) * 100);
				}
			}

			if (myimg == 6) {
				myx -= 20;
				itemx -= 20;
				if (cnt % 4 == 0 || myshoot) {
					myshoot = false;
					itemshoot  = false;
					
					if (bananaItem) {
						shoot = new Bullet(myx + 2500, myy + 1500, 1, 0, RAND(225, 265), 8);
						bullets.add(shoot);
					}
					if(minionItem) {
						shoot = new Bullet(myx + 2500, myy + 1500, 0, 0, RAND(225, 265), 8);
						bullets.add(shoot);
						shoot = new Bullet(myx + 2500, myy + 1500, 3, 0, RAND(225, 265), 8);
						bullets.add(shoot);
						shoot = new Bullet(myx + 2500, myy + 1500, 3, 0, RAND(225, 265), 8);
						bullets.add(shoot);
					}
					else {
						shoot = new Bullet(myx + 2500, myy + 1500, 0, 0, RAND(225, 265), 8);
						bullets.add(shoot);
					}
				}
			}
			break;

		case 3: // 데미지
			// keybuff=0;
			myimg = 8;
			if (mycnt-- == 0) {
				mymode = 0;
				mycnt = 50;
			}
			break;
		}

		if (myx < 2000)
			myx = 2000;
		if (myx > 62000)
			myx = 62000;
		if (myy < 3000)
			myy = 3000;
		if (myy > 45000)
			myy = 45000;
		}
	


	public void process_ENEMY() {
		int i;
		Enemy buff;
		for (i = 0; i < enemies.size(); i++) {
			buff = (Enemy) (enemies.elementAt(i));
			if (!buff.move())
				enemies.remove(i);
		}
	}

	public void process_BULLET() {
		Bullet buff;
		Enemy ebuff;
		Effect expl;
		int i, j, dist;
		for (i = 0; i < bullets.size(); i++) {
			buff = (Bullet) (bullets.elementAt(i));
			buff.move();

			if (buff.dis.x < 10 || buff.dis.x > gScreenWidth + 10
					|| buff.dis.y < 10 || buff.dis.y > gScreenHeight + 10) {
				bullets.remove(i);// 화면 밖으로 나가면 총알 제거
				continue;
			}

			if (buff.from == 0) { // 플레이어가 쏜 총알이 적에게 명중 판정
				for (j = 0; j < enemies.size(); j++) {
					ebuff = (Enemy) (enemies.elementAt(j));
					dist = GetDistance(buff.dis.x, buff.dis.y, ebuff.dis.x, ebuff.dis.y);
					if (dist < 1500) { // 중간점 거리가 명중 판정이 가능한 범위에 왔을 때
						if (bananaItem) {
							ebuff.life = ebuff.life - 7;
							bananaItem = false;
						} else
							ebuff.life--;

						if (ebuff.life <= 0) { // 적 라이프 감소
							enemies.remove(j);// 적 캐릭터 소거
							expl = new Effect(0, ebuff.pos.x, buff.pos.y, 0);
							effects.add(expl);// 폭발 이펙트 추가
							int bbb = RAND(0, 3);
							Item tem1 = new Item(ebuff.pos.x, buff.pos.y, bbb);
							//System.out.println("tem1: " + bbb);
							items.add(tem1);
						}
						expl = new Effect(0, ebuff.pos.x, buff.pos.y, 0);
						effects.add(expl);
						
						score++;// 점수 추가
		                  if(score>150){
		                     level=1;
		                  }
		                  if(score>300){
		                     level=2;
		                  }
		                  
		                  else if(score>400){
		                     level=3;
		                  }
						bullets.remove(i);// 총알 소거   
					}
				}
			}

			else {// 적이 쏜 총알이 플레이어에게 명중 판정
				if (mymode != 2)
					continue;
				dist = GetDistance(myx / 100, myy / 100, buff.dis.x, buff.dis.y);
				if (dist < 500) {
					if (myshield == 0) {
						mymode = 3;
						mycnt = 30;
						bullets.remove(i);
						expl = new Effect(0, myx - 2000, myy, 0);
						effects.add(expl);		
						
						if(minionItem) {
							myMinion--;
							if(myMinion ==0) minionItem=false;
							System.out.println("myMinion : " + myMinion);
						}
						else {
							if (--mylife <= 0) {
								status = 3;
								gamecnt = 0;
							}
						}
					}
					else { // 실드가 있을 경우
						myshield--;
						bullets.remove(i);
					}
				}
			}
		}
	}

	public void process_EFFECT() {
		int i;
		Effect buff;
		for (i = 0; i < effects.size(); i++) {
			buff = (Effect) (effects.elementAt(i));
			if (cnt % 3 == 0)
				buff.cnt--;
			if (buff.cnt == 0)
				effects.remove(i);
		}
	}

	public void process_GAMEFLOW() {
		int control = 0;
		int newy = 0, mode = 0;
		if (gamecnt < 500)
			control = 1;
		else if (gamecnt < 1000)
			control = 2;
		else if (gamecnt < 1300)
			control = 0;
		else if (gamecnt < 1700)
			control = 1;
		else if (gamecnt < 2000)
			control = 2;
		else if (gamecnt < 2400)
			control = 3;
		else {
			gamecnt = 0;
			//level++;
		}

		if (control > 0) {
			newy = RAND(30, gScreenHeight - 30) * 100;
			if (newy < 24000)
				mode = 0;
			else
				mode = 1;
		}

		switch (control) {
		case 1:
			if (gamecnt % 90 == 0) {
				Enemy en = new Enemy(this, 0, gScreenWidth * 100, newy, 0, mode);
				enemies.add(en);
			}
			break;
		case 2:
			if (gamecnt % 50 == 0) {
				Enemy en = new Enemy(this, 0, gScreenWidth * 100, newy, 0, mode);
				enemies.add(en);
			}
			
			break;
		case 3:
			if (gamecnt % 20 == 0) {
				Enemy en = new Enemy(this, 0, gScreenWidth * 100, newy, 0, mode);
				enemies.add(en);
			}	
			break;
		}
	}

	public void process_ITEM() {
		int i, dist;
		Item buff;
		for (i = 0; i < items.size(); i++) {
			buff = (Item) (items.elementAt(i));
			dist = GetDistance(myx / 100, myy / 100, buff.dis.x, buff.dis.y);
			if (dist < 1000) {// 아이템 획득
				switch (buff.kind) {
				case 0:// 일반 득점
					score += 100;
					sc.play();
					break;
				case 1:// 실드
					myshield = 5;
					ss.play();
					break;
				case 2:// 바나나
					bananaItem = true;
					sb.play();
					break;
				case 3: //분신술!_!
					si.play();
					minionItem=true;
					myMinion=3;
					Init_item();
					break;
				}
				items.remove(i);
			} else if (buff.move())
				items.remove(i);
		}
	}

	public Image makeImage(String furl) {
		Image img;
		Toolkit tk = Toolkit.getDefaultToolkit();
		img = tk.getImage(furl);
		try {
			// 여기부터
			MediaTracker mt = new MediaTracker(this);
			mt.addImage(img, 0);
			mt.waitForID(0);
			// 여기까지, getImage로 읽어들인 이미지가 로딩이 완료됐는지 확인하는 부분
		} catch (Exception ee) {
			ee.printStackTrace();
			return null;
		}
		return img;
	}

	public int GetDistance(int x1, int y1, int x2, int y2) {
		return Math.abs((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}

	public int RAND(int startnum, int endnum) {
		 // 랜덤범위(startnum부터 ramdom까지), 랜덤값이 적용될 변수.
		int a, b;
		if (startnum < endnum)
			b = endnum - startnum; // b는 실제 난수 발생 폭
		else
			b = startnum - endnum;
		a = Math.abs(rnd.nextInt() % (b + 1));
		return (a + startnum);
	}

	int getAngle(int sx, int sy, int dx, int dy) {
		int vx = dx - sx;
		int vy = dy - sy;
		double rad = Math.atan2(vx, vy);
		int degree = (int) ((rad * 180) / Math.PI);
		return (degree + 180);
	}

	public boolean readGameFlow(String fname) {
		String buff;
		try {
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			if ((buff = fin.readLine()) != null) {
				System.out.println(Integer.parseInt(buff));
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
