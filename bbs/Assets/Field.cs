using UnityEngine;
using System.Collections;

public class Field : MonoBehaviour {

    #region 변수

    Player[] field;         //수비 야수진
    Player[] basepaths;     //베이스
    int outs;               //아웃카운트

    #endregion

    #region 상수

    /*  상수  */
    const int WALK = 0;
    const int SINGLE = 1;
    const int DOUBLE = 2;
    const int TRIPLE = 3;
    const int HOMERUN = 4;
    const int HBP = 5;
    const int STRIKEOUT = 6;
    const int OUT = 7;
    
    #endregion

    #region 내부함수

    /*  Private 메소드 */

    /// <summary>
    /// 베이스를 클리어하는 메소드
    /// </summary>
    void clearBasepaths()
    {
        for(int i = 0; i < 4; i++)
        {
            basepaths[i] = null;
        }
    }

    /// <summary>
    /// 현재 투수의 투구수를 계산함.
    /// 투수의 삼진, 볼넷, 사구, 아웃 등의 기록에 팩터값을 곱해 투구수를 구함.
    /// 팩터의 조정이 필요
    /// </summary>
    void calcPitchCount()
    {
        field[0].pitchCount = ((4.81 * field[0].so) + (5.14 * field[0].bb) + (3.27 * (field[0].h + field[0].hbp)) + (3.16 * (field[0].to - field[0].so)));
    }

    /// <summary>
    /// 해당 루에 주자가 있는지 체크
    /// </summary>
    /// <param name="i">체크할 루, 1루일 경우 1</param>
    /// <returns>주자가 있을 경우 True, 없을 경우 False</returns>
    bool manOn(int i)
    {
        if (basepaths[i] != null)
            return true;
        else
            return false;
    }

    /// <summary>
    /// 인수로 베이스와 선수를 받아와 해당 베이스에 선수를 위치시킴.
    /// </summary>
    /// <param name="b">베이스, 1루면 1</param>
    /// <param name="player">위치시킬 선수</param>
    void placeRunner(int b, Player player)
    {
        basepaths[b] = player;
    }

    /// <summary>
    /// 선수들을 진루시킬 때 사용하는 함수
    /// </summary>
    /// <param name="b">기준이 되는 베이스</param>
    /// <param name="bases">몇 칸 진루시킬지</param>
    /// <returns></returns>
    int advanceFrom(int b, int bases)
    {
        int runs = 0;
        if ((b + bases) > 3)
        {               //기준 베이스와 진루하는 루의 합이 3을 넘으면 득점임, 득점 상황일 경우
            basepaths[0] = basepaths[b]; //basepaths[0]은 타석임, 타석을 받아온 베이스 넘버의 선수로 교체
            basepaths[b] = null; //받아온 베이스 넘버의 선수자리를 비움
            runs++; //1점 득점
            basepaths[0].r++;//받아온 베이스 선수의 득점을 증가           
        }
        else
        {   //득점 상황이 아닐 경우
            basepaths[b + bases] = basepaths[b];  //기준 위치의 선수를 진루시킨 위치로 바꿈
            basepaths[b] = null;                     //기준 위치를 비움           
        }
        return runs;//진루를 계산하고, 득점이 있을 경우 리턴
    }

    /// <summary>
    /// 해당 루를 비움
    /// </summary>
    /// <param name="b">비울 베이스 번호</param>
    void clearBase(int b)
    {
        basepaths[b] = null;
    }

    /// <summary>
    /// 아웃일 때의 상황을 처리
    /// </summary>
    /// <param name="player">타자</param>
    /// <returns>이 상황을 처리해서 나온 득점</returns>
    private int updateOut(Player player)
    {
        int runs = 0;
        field[0].to++;                                  //투수가 잡은 아웃카운트의 총합 증가
        bool sac = false;                               //태그업 가능한지
        //Random rand = new Random();                   //랜덤머신 생성 - 랜덤머신 Well512로 대체
        int hitDestination = (int)Well512.Next(9);      //타구방향, 타구 방향이 0~8까지 랜덤으로 나오는데 이것도 비율이 있음
        //int hitDestination = rand.nextInt(9);         //타구방향, 타구 방향이 0~8까지 랜덤으로 나오는데 이것도 비율이 있음
        double error = Well512.DoubleNext();            //에러확률
        //double error = rand.nextDouble();             //에러확률
        
        if (error > field[hitDestination].fldPCT)
        {
            //위에서 랜덤으로 나온 에러값이 해당 야수의 파크팩터보다 크면 에러
            field[hitDestination].e++;                  //해당 야수 에러+
            if (manOn(3))                               //3루에 주자가 있을 경우
                runs += advanceFrom(3, 1);              //3루에서 1루 진루한 점수를 더함
            if (manOn(2))                               //2루에 주자가 있을 경우
                runs += advanceFrom(2, 1);              //2루에서 1루 진루한 점수를 더함
            if (manOn(1))                               //1루에 주자가 있을 경우
                runs += advanceFrom(1, 1);              //1루에서 1루 진루한 점수를 더함
            placeRunner(1, player);                     //1루에 인수로 전달받은 선수를 위치시킴
        }

        else if (outs < 2 && hitDestination > 5 && (manOn(3) || manOn(2)))
        {
            //에러가 아니고, 아웃카운트가 2아래이며 타구방향이 외야이며, 1루나 2루에 주자가 있을 경우 = 희생플라이 상황
            if (manOn(3))
            {                                           
                //3루에 주자가 있을 경우                
                runs += advanceFrom(3, 1);              //3루에서 1루 진루한 점수를 더함
                player.rbi++;                           //인수로 받은 선수의 타점 증가
                sac = true;                             //희생타 활성화
            }
            if (manOn(2) && hitDestination == 8)
            {   
                //2루에 주자가 있고, 타구 방향이 중견수일 경우                
                runs += advanceFrom(2, 1);              //2루에서 1루 진루한 점수를 더함
                sac = true;                             //희생타 활성화
            }
            outs++;                                     //아웃카운트 증가
            field[0].to++;                              //투수의 아웃카운트수 증가
        }

        /*	TODO: Implement a better double play model, 병살타 모델 개선 */
        else if (outs < 2 && hitDestination > 1 && hitDestination < 6 && manOn(1))
        {
            //아웃카운트가 2개 이하고, 타구 방향이 1이상, 2,3,4,5 방향, 즉 내야이며, 1루에 주자가 있을 경우 = 더블플레이 상황
                           
            if (outs + 2 == 3)
            {                                       //아웃카운트가 1일 경우
                outs += 2;                          //아웃카운트 2개 추가
                field[0].to += 2;                   //투수의 아웃카운트 2개 추가
            }
            else
            {                                       //아웃카운트가 0인 경우
                if (manOn(3))
                {                                   //3루에 주자가 있을 경우
                    runs += advanceFrom(3, 1);      //득점에 3루에서 1루 진루한 점수를 더함
                }
                if (manOn(2))                       //2루에 주자가 있을 경우
                    runs += advanceFrom(2, 1);      //득점에 2루에서 1루 진루한 점수를 더함
                clearBase(2);                       //2루를 비움
                clearBase(1);                       //1루를 비움
                field[0].to += 2;                   //투수의 아웃카운트에 2를 더함
                outs += 2;                          //아웃카운트에 2를 더함
            }
        }
        else                                        //이외의 경우
            outs++;                                 //아웃카운트 증가



        if (sac == true)                            //희생타 상황이면
        {/* do nothing */ }                         //타수를 증가시키지 않음           
        else                                        //아닐 경우
            player.ab++;                            //타수를 증가시킴

        return runs;                                //득점을 반환하면서 메소드 종료
    }

    /// <summary>
    /// 단타일 때의 상황을 처리
    /// </summary>
    /// <param name="player">타자</param>
    /// <returns>이 상황을 처리해서 나온 득점</returns>
    private int updateSingle(Player player)
    {
        int runs = 0;                               //반환할 점수 선언
        field[0].h++;                               //투수의 피안타 증가

        if (manOn(3))
        {   //3루에 주자가 있을 경우
            runs += advanceFrom(3, 1);              //득점에 3루에서 1루 진루한 점수를 더함
            player.rbi++;                           //타자 타점 증가
            field[0].er++;                          //투수의 자책점 증가
        }

        if (manOn(2))
        {   //2루에 주자가 있는 경우
            if (player.isFast())
            {   //타자가 빠른 경우, 근데 이건 2루 주자의 속도를 체크해야함
                runs += advanceFrom(2, 2);          //득점에 2루에서 2루 진루
                player.rbi++;                       //타자의 타점 증가
                field[0].er++;                      //투수의 자책점 증가
            }
            else
                runs += advanceFrom(2, 1);          //득점에 2루에서 1루 진루
        }
        if (manOn(1))
        {   //1루에 주자가 있는 경우
            if (!manOn(3) && player.isFast())       //3루에 주자가 없고, 타자가 빠른 경우, 이 경우에도 주자의 속도를 체크해야함
                runs += advanceFrom(1, 2);          //1루에서 2루 진루
            else
                runs += advanceFrom(1, 1);          //느린 경우 1루에서 1루 진루
        }
        placeRunner(1, player);                     //타자를 1루에 위치시킴
        player.ab++;                                //타자의 타수 증가
        player.hit++;                               //타자의 안타 증가

        return runs;								//득점 반환
    }

    /// <summary>
    /// 2루타일 때의 상황을 처리
    /// </summary>
    /// <param name="player">타자</param>
    /// <returns>이 상황을 처리해서 나온 득점</returns>
    int updateDouble(Player player)
    {
        int runs = 0;
        field[0].h++;                               //투수 피안타 증가

        if (manOn(3))
        {   //3루에 주자가 있을 경우
            runs += advanceFrom(3, 2);              //3루에서 2루 진루
            player.rbi++;                           //타자 타점 증가
            field[0].er++;                          //투수의 자책점 증가
        }

        if (manOn(2))
        {   //2루에 주자가 있을 경우
            runs += advanceFrom(2, 2);              //2루에서 2루 진루
            player.rbi++;                           //타자의 타점 증가
            field[0].er++;                          //투수의 자책점 증가
        }

        if (manOn(1))
        {   //1루에 주자가 있을 경우
            if (player.isFast())
            {   //타자가 빠를 경우, 이것도 주자의 속도를 체크해야함
                runs += advanceFrom(1, 3);          //1루에서 3루 진루
                player.rbi++;                       //타자 타점 증가
                field[0].er++;                      //투수 자책점 증가
            }
            else                                    //타자가 느릴 경우
                runs += advanceFrom(1, 2);          //1루에서 2루 진루
        }

        placeRunner(2, player);                     //타자를 2루에 위치
        player.ab++;                                //타자의 타수 증가
        player.hit++;                               //타자의 안타 증가

        return runs;								//득점 반환
    }

    /// <summary>
    /// 3루타일 때의 상황을 처리
    /// </summary>
    /// <param name="player">타자</param>
    /// <returns>이 상황을 처리해서 나온 득점</returns>
    int updateTriple(Player player)
    {
        int runs = 0;
        field[0].h++;                               //투수의 피안타 증가

        if (manOn(3))
        {   //3루에 주자가 있을 경우
            runs += advanceFrom(3, 3);              //3루에서 3칸 진루
            player.rbi++;                           //타자의 타점 증가
            field[0].er++;                          //투수의 자책점 증가
        }

        if (manOn(2))
        {   //2루에 주자가 있을 경우
            runs += advanceFrom(2, 3);              //2루에서 3칸 진루
            player.rbi++;                           //타자의 타점 증가
            field[0].er++;                          //투수의 자책점 증가
        }

        if (manOn(1))
        {   //1루에 주자가 있을 경우
            runs += advanceFrom(1, 3);              //1루에서 3칸 진루
            player.rbi++;                           //타자의 타점 증가
            field[0].er++;                          //투수의 자책점 증가
        }

        placeRunner(3, player);                     //타자를 3루에 위치
        player.ab++;                                //타자의 타수 증가
        player.hit++;                               //타자의 안타 증가
        return runs;								//득점 반환
    }

    /// <summary>
    /// 홈런일 때의 상황을 처리
    /// </summary>
    /// <param name="player"></param>
    /// <returns></returns>
    int updateHomerun(Player player)
    {
        int runs = 0;
        field[0].h++;                               //투수의 피안타 증가
        field[0].er++;                              //투수의 자책점 증가

        if (manOn(3))
        {                           //3루에 주자가 있을 경우
            runs += advanceFrom(3, 4);              //3루에서 4칸 진루
            player.rbi++;                           //타자의 타점 증가
            field[0].er++;                          //투수의 자책점 증가
        }

        if (manOn(2))
        {                       //2루에 주자가 있을 경우
            runs += advanceFrom(2, 4);              //2루에서 4칸 진루
            player.rbi++;                           //타자의 타점 증가
            field[0].er++;                          //투수의 자책점 증가
        }

        if (manOn(1))
        {                           //1루에 주자가 있을 경우
            runs += advanceFrom(1, 4);              //1루에서 4칸 진루
            player.rbi++;                           //타자의 타점 증가
            field[0].er++;                          //투수의 자책점 증가
        }

        player.rbi++;                               //타자의 타점 증가
        player.ab++;                                //타자의 타수 증가
        player.r++;                                 //타자의 득점 증가
        
        player.hit++;                               //타자의 안타 증가
        runs++;                                     //득점 추가
        return runs;								//득점 반환
    }

    /// <summary>
    /// 삼진일 때의 상황을 처리
    /// </summary>
    /// <param name="player"></param>
    /// <returns></returns>
    int updateStrikeout(Player player)
    {
        int runs = 0;
        field[0].so++;                              //투수의 삼진 증가
        field[0].to++;                              //투수의 아웃 증가

        double error = Well512.DoubleNext();           //에러확률 계산

        if (error > field[1].fldPCT)
        {               //에러확률이 팩터보다 크게 나왔으면
                        // catcher makes an error: passed ball
            field[1].e++;                           //포수의 에러 증가
            

            //각 루에 주자가 있었을 경우 한 루씩 진루
            if (manOn(3))
                runs += advanceFrom(3, 1);
            if (manOn(2))
                runs += advanceFrom(2, 1);
            if (manOn(1))
                runs += advanceFrom(1, 1);

            placeRunner(1, player);//타자를 1루에 위치	
        }
        else
            outs++;                                 //에러가 안났을 경우 아웃증가
        player.ab++;                                //타자의 타수 증가
        return runs;
    }
    

    int updateWalk(Player player)
    {
        int runs = 0;                               //이 상황이 끝났을 때의 득점 결과
        field[0].bb++;                              //투수의 볼넷카운트를 증가
        if (manOn(3))                           //3루에 선수가 위치했을 경우
            if (manOn(2) && manOn(1))
            {   //1, 2루에도 선수가 있을 경우
                runs += advanceFrom(3, 1);          //타자가 3루에서 1루 진루했을 때 점수의 상황
                player.rbi++;                       //타자의 타점을 증가시킴.
                field[0].er++;                      //투수의 자책점을 증가시킴
            }
        if (manOn(2))
        {                       //2루에 선수가 위치했을 경우
            if (manOn(1))
            {                       //1루에 선수가 위치했을 경우
                runs += advanceFrom(2, 1);          //2루에서 1루 진루했을 때 점수의 상황
            }
        }
        if (manOn(1))
        {                           //1루에 선수가 위치했을 경우
            runs += advanceFrom(1, 1);              //1루에서 1루 진루했을 때 점수의 상황
        }
        placeRunner(1, player);                     //인수로 전달받은 선수를 1루에 위치시킴
        return runs;								//득점 결과를 반환
    }

    int updateHBP(Player player)
    {
        int runs = 0;
        field[0].hbp++;                             //투수의 사구 증가

        if (manOn(3))                           //주자가 3루에 위치했을 경우
            if (manOn(2) && manOn(1))
            {   //주자가 1루, 2루에도 있을 경우
                runs += advanceFrom(3, 1);          //3루에서 1칸 진루
                player.rbi++;                       //타자의 타점 증가
                field[0].er++;//					//투수의 자책점 증가
            }

        if (manOn(2))                          //주자가 2루에 있을 경우
            if (manOn(1))
            {                       //주자가 1루에 있을 경우
                runs += advanceFrom(2, 1);          //주자를 2루에서 1칸 진루
            }

        if (manOn(1))
        {                           //주자가 1루에 있을 경우
            runs += advanceFrom(1, 1);              //주자를 1루에서 1칸 진루
        }

        placeRunner(1, player);                     //타자를 1루에 위치시킴
        return runs;
    }

    #endregion

    #region 공용함수

    /*  Public 메소드  */

    /// <summary>
    /// 생성자
    /// </summary>
    public Field()
    {
        basepaths = new Player[4];
        outs = 0;
    }

    public void ResetField(Player[] init_field)
    {
        field = init_field;
        clearBasepaths();
        outs = 0;
    }

    /// <summary>
    /// 현재 아웃카운트를 가져옴
    /// </summary>
    /// <returns>현재 아웃 카운트</returns>
    public int getOuts()
    {
        return outs;
    }

    public int updateField(int result, Player player)
    {
        //필드를 업데이트하는 함수, 인수로 타격 결과와 선수정보를 받아옴
        if (result == WALK)
        {                       //결과가 볼넷일 경우            
            calcPitchCount();                       //투구수를 계산하는 메소드
            return updateWalk(player);              //볼넷으로 인한 득점결과를 반환함
        }
        else if (result == HBP)
        {            
            calcPitchCount();
            return updateHBP(player);
        }
        else if (result == STRIKEOUT)
        {
            calcPitchCount();
            return updateStrikeout(player);
        }
        else if (result == HOMERUN)
        {
            calcPitchCount();
            return updateHomerun(player);
        }
        else if (result == TRIPLE)
        {
            calcPitchCount();
            return updateTriple(player);
        }
        else if (result == DOUBLE)
        {
            calcPitchCount();
            return updateDouble(player);
        }
        else if (result == SINGLE)
        {   //여기까지 전부 해당 결과로 인한 투구수정리, 득점결과를 반환함
            calcPitchCount();
            return updateSingle(player);
        }
        else
        {   //아웃으로 인한 득점 결과를 반환            
            calcPitchCount();
            return updateOut(player);
        }
    }

    #endregion
}
