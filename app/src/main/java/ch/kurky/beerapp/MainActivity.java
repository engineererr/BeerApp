package ch.kurky.beerapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasClientException;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasException;
import com.baasbox.android.BaasObject;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasServerException;
import com.baasbox.android.BaasUser;
import com.baasbox.android.Plugin;
import com.baasbox.android.json.JsonObject;
import com.baasbox.android.plugins.glide.GlidePlugin;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lvItem;
    private ArrayAdapter<BaasDocument> adapter;
    private ListTask listTask;
    private DeleteTask deleteTask;

    private BaasBox box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize BaasBox for the whole App
        BaasBox.Builder b = new BaasBox.Builder(this);
        b.setApiDomain("192.168.0.101").setAppCode("1234567890").setPort(9000).setAuthentication(BaasBox.Config.AuthType.SESSION_TOKEN);
        b.init();


        if ( !BaasUser.current().isAuthentcated()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        lvItem = (ListView)this.findViewById(R.id.lvQueue);
        adapter = new Adapter(this);
        lvItem.setAdapter(adapter);
        lvItem.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BaasDocument beer = adapter.getItem(position);
                // when beer is ordered it can't be canceled
                if(beer.getBoolean("ordered")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("Already ordered");
                    builder.setMessage("This beer is already ordered.");
                    builder.setNegativeButton("OK, I drink it", null);
                    builder.create().show();
                }else{
                    delete(beer);
                }


            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BeerActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                BaasDocument beer = data.getParcelableExtra("item");

                adapter.add(beer);
                adapter.notifyDataSetChanged();
            }
            else if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public class Adapter extends ArrayAdapter<BaasDocument> {

        public Adapter(Context context) {
            super(context, android.R.layout.simple_list_item_2,	new ArrayList<BaasDocument>());
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_2,
                        null);

                Tag tag = new Tag();
                tag.text1 = (TextView) view.findViewById(android.R.id.text1);
                tag.text2 = (TextView) view.findViewById(android.R.id.text2);
                view.setTag(tag);
            }

            Tag tag = (Tag) view.getTag();
            BaasDocument beer = getItem(position);
            if(beer.getBoolean("ordered")){
                view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                tag.text1.setTextColor(Color.WHITE);
                tag.text2.setTextColor(Color.WHITE);
                tag.text1.setText(beer.getString("name"));
                tag.text2.setText("CHF " + beer.getString("price"));
            }else{
                tag.text1.setText(beer.getString("name"));
                tag.text2.setText("CHF " + beer.getString("price"));
            }


            return view;
        }

    }

    protected static class Tag {

        public TextView text1;
        public TextView text2;

    }

    private void refresh() {
        listTask = new MainActivity.ListTask();
        listTask.execute();
    }

    public class ListTask extends AsyncTask<Void, Void, BaasResult<List<BaasDocument>>> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected BaasResult<List<BaasDocument>> doInBackground(Void... params) {
            return BaasDocument.fetchAllSync("queue");
        }

        @Override
        protected void onPostExecute(BaasResult<List<BaasDocument>> result) {
            onListReceived(result);
        }
    }

    protected void onListReceived(BaasResult<List<BaasDocument>> result) {
        try {
            List<BaasDocument> array = result.get();
            adapter.clear();
            for (int i = 0; i < array.size(); i++) {
                adapter.add(array.get(i));
            }
            adapter.notifyDataSetChanged();
        } catch (BaasClientException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Error");
            builder.setMessage("Error: " + e);
            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        } catch (BaasServerException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Error");
            builder.setMessage("Error: " + e);
            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        } catch (BaasException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Error");
            builder.setMessage("Error: " + e);
            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        }
    }

    protected void delete(BaasDocument beer) {
        adapter.remove(beer);
        new DeleteTask().execute(beer);
    }

    public class DeleteTask extends	AsyncTask<BaasDocument, Void, BaasResult<Void>> {

        @Override
        protected BaasResult<Void> doInBackground(BaasDocument... params) {
            return params[0].deleteSync();
        }

        @Override
        protected void onPostExecute(BaasResult<Void> result) {
            onBeerDeleted(result);
        }
    }

    protected void onBeerDeleted(BaasResult<Void> result) {
        try {
            result.get();
        } catch (BaasClientException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Error");
            builder.setMessage("Error: " + e);
            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        } catch (BaasServerException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Error");
            builder.setMessage("Error: " + e);
            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        } catch (BaasException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Error");
            builder.setMessage("Error: " + e);
            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        }
    }
}
