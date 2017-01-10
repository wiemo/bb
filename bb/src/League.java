/* 	League class for BaseballSim
	by Ian Zapolsky 6/6/13 */

import java.util.ArrayList;
import java.util.Scanner;

public class League {

	//���� ��ġ ����
	int year;							//�⵵
	double BF;							//���Ÿ�ڼ�
	double Hit;							//��Ÿ
	double singles;						//��Ÿ
	double doubles;						//2��Ÿ
	double triples;						//3��Ÿ
	double HR;							//Ȩ��
	double BB; 							//����
	double SO;							//����
	double HBP;							//�籸
	
	//������ս���
	double leagueOVASingle;				//������մ�Ÿ��
	double leagueOVADouble; 
	double leagueOVATriple;
	double leagueOVAHR;
	double leagueOVABB; 
	double leagueOVASO;
	double leagueOVAHBP;

	public League() { }

	public void setLeagueOVA() {
		leagueOVASingle = singles/BF;	//���� ��� ��Ÿ�� = ��Ÿ/���Ÿ��
		leagueOVADouble = doubles/BF;	//���� ��� 2��Ÿ�� = 2��Ÿ / ���Ÿ��
		leagueOVATriple = triples/BF;
		leagueOVAHR = HR/BF;
		leagueOVABB = BB/BF;
		leagueOVASO = SO/BF;
		leagueOVAHBP = HBP/BF;
	}

	public void setYear(int init_year) {	//�μ��� �޾ƿ� ������ ������ ����
		year = init_year;
	}

	public String toString() {
		return String.valueOf(year);
	}

}
