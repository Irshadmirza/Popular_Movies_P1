package movies.popular.baig.vaagdevi.hanamkonda.warangal.mirza.irshad.popularmovies.movieproject1;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import movies.popular.baig.vaagdevi.hanamkonda.warangal.mirza.irshad.popularmovies.model.Movie;

/**
 * Created by irshadmirza on 03/03/16.
 */


public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_DATA = "movie_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        Movie item = intent.getParcelableExtra(EXTRA_DATA);
        if (savedInstanceState == null) {
            Fragment fragment = MovieDetailFragment.getInstance(item);
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment, null).commit();
        }

    }
}
