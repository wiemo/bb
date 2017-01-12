using UnityEngine;
using System.Collections;

public class ScoreKeeper : MonoBehaviour {

    Team home, away;
    Player[] homeOrder, awayOrder;
    double homeRuns, awayRuns, homeHits, awayHits, homeErrors;
    double awayErrors, homeAVGRuns, awayAVGRuns, homeAVGHits;
    double awayAVGHits, homeAVGErrors, awayAVGErrors;
    double iterations, homeWins, awayWins, homeAVGWins, awayAVGWins;



    public ScoreKeeper()
    {
        homeRuns = awayRuns = homeHits = awayHits = homeErrors = awayErrors = homeAVGRuns = awayAVGRuns = homeAVGHits = awayAVGHits = 0;
        iterations = homeAVGErrors = awayAVGErrors = homeWins = awayWins = homeAVGWins = awayAVGWins = 0;
    }

    public void updateMultisimStats(Game game)
    {
        iterations++;
        home = game.home;
        away = game.away;
        homeOrder = game.homeOrder;
        awayOrder = game.awayOrder;

        homeRuns += game.hRuns;
        homeAVGRuns = homeRuns / iterations;
        awayRuns += game.aRuns;
        awayAVGRuns = awayRuns / iterations;

        if (game.hRuns > game.aRuns)
            homeWins++;
        else
            awayWins++;

        homeAVGWins = homeWins / iterations;
        awayAVGWins = awayWins / iterations;

        updateHomeStats();
        updateAwayStats();
    }

    private void updateHomeStats()
    {
        foreach(Player p in homeOrder)
        {

            homeErrors += p.e;
            homeHits += p.hit;
        }
        homeAVGErrors = homeErrors / iterations;
        homeAVGHits = homeHits / iterations;
    }

    private void updateAwayStats()
    {
        foreach(Player p in awayOrder)
        {
            awayErrors += p.e;
            awayHits += p.hit;
        }
        awayAVGErrors = awayErrors / iterations;
        awayAVGHits = awayHits / iterations;
    }

    public string generateMultisimStats()
    {
        string result = ("------------------------------------------------------------\n");
        result += (home + " VS. " + away + "\n");
        result += (iterations + " iterations:\n");
        result += ("------------------------------------------------------------\n\n");

        result += "HOME: " + home + "\n";
        result += "total wins: " + homeWins + "\n";
        result += "win %: " + homeAVGWins + "\n";
        result += "total runs: " + homeRuns + "\n";
        result += "runs/game: " + homeAVGRuns + "\n";
        result += "total hits: " + homeHits + "\n";
        result += "hits/game: " + homeAVGHits + "\n";
        result += "total errors: " + homeErrors + "\n";
        result += "errors/game: " + homeAVGErrors + "\n\n";

        result += "AWAY: " + away + "\n";
        result += "total wins: " + awayWins + "\n";
        result += "win %: " + awayAVGWins + "\n";
        result += "total runs: " + awayRuns + "\n";
        result += "runs/game: " + awayAVGRuns + "\n";
        result += "total hits: " + awayHits + "\n";
        result += "hits/game: " + awayAVGHits + "\n";
        result += "total errors: " + awayErrors + "\n";
        result += "errors/game: " + awayAVGErrors + "\n\n";
        return result;
    }


    public string generateLineScore()
    {
        string result = "";
        result += (home + " VS. " + away + "\n");
        result += ("LINE SCORE\n");
        result += (away + teamTab(away) + "AB R  H  RBI\n");
        foreach(Player p in awayOrder)
            result += (p + playerTab(p) + ((int)p.ab) + "  " + ((int)p.r) + "  " + ((int)p.hit) + "  " + ((int)p.rbi) + "\n");
        result += ("\n");
        result += (home + teamTab(home) + "AB R  H  RBI\n");
        foreach (Player p in homeOrder)
            result += (p + playerTab(p) + ((int)p.ab) + "  " + ((int)p.r) + "  " + ((int)p.hit) + "  " + ((int)p.rbi) + "\n");
        return result;
    }

    private string teamTab(Team t)
    {
        int length = t.ToString().Length;
        int tabsNeeded;
        string tabs = "";
        if (length <= 15)
            tabsNeeded = 3;
        else if (length <= 25)
            tabsNeeded = 2;
        else
            tabsNeeded = 1;
        for (int i = 0; i < tabsNeeded; i++)
            tabs += "\t";
        return tabs;
    }

    private string playerTab(Player p)
    {
        int length = p.ToString().Length;
        int tabsNeeded;
        string tabs = "";
        if (length <= 15)
            tabsNeeded = 3;
        else if (length <= 25)
            tabsNeeded = 2;
        else
            tabsNeeded = 1;
        for (int i = 0; i < tabsNeeded; i++)
            tabs += "\t";
        return tabs;
    }
}
