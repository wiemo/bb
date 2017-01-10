/*	Team class for BaseballSim
	by Ian Zapolsky 6/4/13 */

import java.util.ArrayList;

public class Team {

	String name;				//���̸�
	int year;					//�⵵
	ArrayList<Player> roster;	//�� �ν���, �÷��̾� �迭����Ʈ
	ArrayList<Player> bullpen;	//�� ����, �÷��̾� �迭����Ʈ
		
	public Team() {				//�� Ŭ���� ������
		roster = new ArrayList<Player>();	//�� �ν��� �ʱ�ȭ
		bullpen = new ArrayList<Player>();	//�� ���� �ʱ�ȭ
	}

	public void setInfo(String info) {
		year = Integer.valueOf(info.substring(4,8));
		name = info.substring(9,(info.length()-5));
	}

	public void addPlayer(Player input) {	//�����߰� �Լ�, �μ��� ��������
		roster.add(input);					//�ν��Ϳ� ���� �߰���
		if (input.position.equals("P"))		//���� ������ ���濡�� �߰�
			bullpen.add(input);
	}

	public void zeroOut() {					//�ν��Ϳ� �ִ� ��� ������ ���ӽ��� �ʱ�ȭ
		for (Player p : roster)
			p.clearGameStats();
	}

	public Player getPlayer(String name) {	//�μ��� ���� �̸��� ������ �����ϴ� �Լ�
		Player target = roster.get(0);
		for (Player p : roster) {
			if (p.name.equals(name))
				target = p;
		}
		return target;
	}

	public void setAverages() {				//���� ���� ��հ��
		for (Player p : roster)
			p.setAvg();
	}

	public String printRoster() {			//�ν��� ���� �������� �����
		String result = ""+year+" "+name+"\n";
		result += "ROSTER:\n";
		for (Player i : roster)
			result += i+" ("+i.position+")\n";
		return result;
	}

	public String toString() {
		return ""+name+" ("+year+")";
	}
}
