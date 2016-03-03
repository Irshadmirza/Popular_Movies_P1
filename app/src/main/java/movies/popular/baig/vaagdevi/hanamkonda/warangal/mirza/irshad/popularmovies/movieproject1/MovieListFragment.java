package movies.popular.baig.vaagdevi.hanamkonda.warangal.mirza.irshad.popularmovies.movieproject1;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import movies.popular.baig.vaagdevi.hanamkonda.warangal.mirza.irshad.popularmovies.model.Movie;
import movies.popular.baig.vaagdevi.hanamkonda.warangal.mirza.irshad.popularmovies.adapter.MovieAdapter;
import java.util.ArrayList;


/**
 * Created by irshadmirza on 03/03/16.
 */


public class MovieListFragment extends Fragment implements AdapterView.OnItemClickListener, MovieTask.OnTaskStatusListener {


    public interface ShowDetailListener{
        void onShowDetail(Movie movie);
    }

    private static final String TAG = MovieListFragment.class.getSimpleName();

    //Keys  for saving data in onSavedInstanceState()
    public static final String SAVED_DATA = "saved_data";
    public static final String SCROLL_POSITION = "scroll_position";


    public static final String SORT_BY = "sort_by";
    public static final int POPULARITY = 1;
    public static final int HIGH_RATING = 2;

    private ShowDetailListener mShowDetailListener;


    private GridView mGridView;
    private ProgressBar mProgressBar;
    private TextView mEmptyView;
    private LinearLayout mProgressContainer;
    private MovieAdapter mAdapter;
    private ArrayList<Movie> mList;


    public static MovieListFragment getInstance() {
        return new MovieListFragment();
    }

    public MovieListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity() instanceof ShowDetailListener) {
            mShowDetailListener = (ShowDetailListener) getActivity();
        }else {
            throw new ClassCastException("Activity must implements "+ShowDetailListener.class.getSimpleName()+" interface");
        }


        mList = new ArrayList<>();
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movie_list_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);

        int sort_by = getCurrentSortOrder();
        if (sort_by == POPULARITY) {
            menu.findItem(R.id.action_popularity).setChecked(true);
            menu.findItem(R.id.action_high_rating).setChecked(false);
        } else {
            menu.findItem(R.id.action_popularity).setChecked(false);
            menu.findItem(R.id.action_high_rating).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_popularity:
                setCurrentSortOrder(POPULARITY);
                reloadListData(POPULARITY);
                break;
            case R.id.action_high_rating:
                setCurrentSortOrder(HIGH_RATING);
                reloadListData(HIGH_RATING);
                break;
        }
        getActivity().invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }

    public void setCurrentSortOrder(int sort_by){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        if (sort_by == POPULARITY) {
            editor.putInt(SORT_BY, POPULARITY);
        }else{
            editor.putInt(SORT_BY, HIGH_RATING);
        }
        editor.commit();

    }

    public int getCurrentSortOrder(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return preferences.getInt(SORT_BY, POPULARITY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.list);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressbar);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty);
        mProgressContainer = (LinearLayout) rootView.findViewById(R.id.progressContainer);
        mProgressContainer.setVisibility(View.GONE);

        mEmptyView.setVisibility(View.GONE);

        mAdapter = new MovieAdapter(getActivity(), mList);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SAVED_DATA, mList);
        outState.putInt(SCROLL_POSITION, mGridView.getFirstVisiblePosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_DATA)) {
            ArrayList<Movie> list = savedInstanceState.getParcelableArrayList(SAVED_DATA);
            resetAdapter(list);
            int position = savedInstanceState.getInt(SCROLL_POSITION);
            if (position >= 0) {
                mGridView.smoothScrollToPosition(position);
            }
        } else {
            int sort_by = getCurrentSortOrder();
            reloadListData(sort_by);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Movie item = mAdapter.getItem(position);
        if (mShowDetailListener != null) {
            mShowDetailListener.onShowDetail(item);
        }
    }


    private void reloadListData(int sort_by) {
        if (sort_by == POPULARITY) {
            new MovieTask(this).execute(Api.JSON.POPULAR_MOVIE_URL);
        } else {
            new MovieTask(this).execute(Api.JSON.HIGH_RATED_MOVIE_URL);
        }
    }


    // resetting the adapter data with a new list
    private void resetAdapter(ArrayList<Movie> movies) {
        if (movies != null) {
            mAdapter.clear();
            for (Movie item : movies) {
                mAdapter.add(item);
            }
        }
        if (mAdapter.getCount() <= 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onTaskStart() {
        mProgressContainer.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }

    @Override
    public void onResponseReceived(JSONObject response) {
        if(response == null) {
            resetAdapter(new ArrayList<Movie>());
            return;
        }

        ArrayList<Movie> list = new ArrayList<>();
        try {

            JSONArray results = response.getJSONArray(Api.JSON.RESULTS);
            for (int i = 0; i < results.length(); i++) {
                JSONObject movie = results.getJSONObject(i);
                Movie item = new Movie(movie);
                list.add(item);
            }
            resetAdapter(list);
        }catch (JSONException e) {
            Log.d(TAG, "Error in Parsing data");
        }
    }


    @Override
    public void onTaskEnd() {
        mProgressContainer.setVisibility(View.GONE);
    }
}
