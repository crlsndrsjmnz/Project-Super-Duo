package it.jaschke.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;


public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int INDEX_TITLE = 0;
    public static final int INDEX_SUBTITLE = 1;
    public static final int INDEX_IMAGE_URL = 2;
    public static final int INDEX_DESC = 3;
    public static final int INDEX_AUTHOR = 4;
    public static final int INDEX_CATEGORY = 5;

    private static final String LOG_TAG = BookDetail.class.getSimpleName();

    public static final String EAN_KEY = "EAN";
    private final int LOADER_ID = 10;
    private View rootView;
    private String ean;
    private String bookTitle;
    private ShareActionProvider shareActionProvider;

    TextView mTvBookTitle;
    TextView mTvBookSubTitle;
    TextView mTvBookDescription;
    TextView mTvBookAuthor;
    TextView mTvBookCategories;

    ImageView mIvBookCover;

    public BookDetail() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            ean = arguments.getString(BookDetail.EAN_KEY);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mTvBookTitle = (TextView) rootView.findViewById(R.id.fullBookTitle);
        mTvBookSubTitle = (TextView) rootView.findViewById(R.id.fullBookSubTitle);
        mTvBookDescription = (TextView) rootView.findViewById(R.id.fullBookDesc);
        mTvBookAuthor = (TextView) rootView.findViewById(R.id.authors);
        mTvBookCategories = (TextView) rootView.findViewById(R.id.categories);

        mIvBookCover = (ImageView) rootView.findViewById(R.id.fullBookCover);

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        Intent shareIntent = createShareForecastIntent();
        if (shareActionProvider != null && shareIntent != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst())
            return;

        bookTitle = data.getString(INDEX_TITLE);
        mTvBookTitle.setText(bookTitle);

        Intent shareIntent = createShareForecastIntent();
        if (shareActionProvider != null && shareIntent != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }

        String bookSubTitle = data.getString(INDEX_SUBTITLE);
        mTvBookSubTitle.setText(bookSubTitle);

        String desc = data.getString(INDEX_DESC);
        mTvBookDescription.setText(desc);

        String authors = data.getString(INDEX_AUTHOR);
        if (authors != null && authors.length() > 0) {
            String[] authorsArr = authors.split(",");
            mTvBookAuthor.setLines(authorsArr.length);
            mTvBookAuthor.setText(authors.replace(",", "\n"));
        } else {
            mTvBookAuthor.setText("");
        }

        String imgUrl = data.getString(INDEX_IMAGE_URL);
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            Glide.with(getActivity())
                    .load(imgUrl)
                    .error(R.drawable.ic_launcher)
                    .crossFade()
                    .into(mIvBookCover);
        }

        String categories = data.getString(INDEX_CATEGORY);
        mTvBookCategories.setText(categories);

        if (rootView.findViewById(R.id.right_container) != null) {
            rootView.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void onPause() {
        super.onDestroyView();
        if (MainActivity.IS_TABLET && rootView.findViewById(R.id.right_container) == null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private Intent createShareForecastIntent() {
        if (bookTitle != null && !bookTitle.isEmpty()) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);
            return shareIntent;
        }
        return null;
    }
}