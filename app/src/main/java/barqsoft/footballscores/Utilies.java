package barqsoft.footballscores;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import barqsoft.footballscores.service.FetchService;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies {
    // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
    // be updated.  (http://api.football-data.org/v1/soccerseasons)
    public static final int BL1 = 394;
    public static final int BL2 = 395;
    public static final int FL1 = 396;
    public static final int FL2 = 397;
    public static final int PL = 398;
    public static final int PD = 399;
    public static final int SD = 400;
    public static final int SA = 401;
    public static final int PPL = 402;
    public static final int BL3 = 403;
    public static final int DED = 404;
    public static final int CL = 405;
    public static final int EL1 = 425;

    public static String getLeague(int league_num, Context context)
    {
        switch (league_num)
        {
            case BL1 : return context.getString(R.string.league_Bundesliga1);
            case BL2 : return context.getString(R.string.league_Bundesliga2);
            case FL1 : return context.getString(R.string.league_Ligue1);
            case FL2 : return context.getString(R.string.league_Ligue2);
            case PL : return context.getString(R.string.league_PremierLeague);
            case PD : return context.getString(R.string.league_PrimeraDivision);
            case SD : return context.getString(R.string.league_SegundaDivision);
            case SA : return context.getString(R.string.league_SerieA);
            case PPL : return context.getString(R.string.league_PrimeiraLiga);
            case BL3 : return context.getString(R.string.league_Bundesliga3);
            case DED : return context.getString(R.string.league_Eredivisie);
            case CL : return context.getString(R.string.league_ChampionsLeague);
            case EL1 : return context.getString(R.string.league_LeagueOne);
            default: return context.getString(R.string.league_NotKnown);
        }
    }
    public static String getMatchDay(Context context, int match_day,int league_num)
    {
        if(league_num == CL)
        {
            if (match_day <= 6)
            {
                return context.getString(R.string.matchday_groupStages);
            }
            else if(match_day == 7 || match_day == 8)
            {
                return context.getString(R.string.matchday_knockout);
            }
            else if(match_day == 9 || match_day == 10)
            {
                return context.getString(R.string.matchday_quarterfinal);
            }
            else if(match_day == 11 || match_day == 12)
            {
                return context.getString(R.string.matchday_semifinal);
            }
            else
            {
                return context.getString(R.string.matchday_final);
            }
        }
        else
        {
            return context.getString(R.string.matchday_other) + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName (Context context, String teamname){
        if (teamname==null){return R.drawable.no_icon;}
        //This is the set of icons that are currently in the app. Feel free to find and add more
        //as you go.
        if(teamname.equals(context.getString(R.string.teamName_Arsenal))){ return R.drawable.arsenal;}
        else if(teamname.equals(context.getString(R.string.teamName_Manchester))){ return R.drawable.manchester_united;}
        else if(teamname.equals(context.getString(R.string.teamName_Swansea))){ return R.drawable.swansea_city_afc;}
        else if(teamname.equals(context.getString(R.string.teamName_Leicester))){ return R.drawable.leicester_city_fc_hd_logo;}
        else if(teamname.equals(context.getString(R.string.teamName_Everton))){ return R.drawable.everton_fc_logo1;}
        else if(teamname.equals(context.getString(R.string.teamName_WestHam))){ return R.drawable.west_ham;}
        else if(teamname.equals(context.getString(R.string.teamName_Tottenham))){ return R.drawable.tottenham_hotspur;}
        else if(teamname.equals(context.getString(R.string.teamName_WestBromwich))){ return R.drawable.west_bromwich_albion_hd_logo;}
        else if(teamname.equals(context.getString(R.string.teamName_Sunderland))){ return R.drawable.sunderland;}
        else if(teamname.equals(context.getString(R.string.teamName_Stoke))){ return R.drawable.stoke_city;}
        else{ return R.drawable.no_icon;}
    }

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /**
     *
     * @param c Context used to get the SharedPreferences
     * @return the scores status integer type
     */
    @SuppressWarnings("ResourceType")
    static public @FetchService.ScoresStatus
    int getScoresStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_scores_status_key), FetchService.SCORES_STATUS_UNKNOWN);
    }
}
