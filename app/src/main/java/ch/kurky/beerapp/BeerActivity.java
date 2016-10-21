package ch.kurky.beerapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasClientException;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasException;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasObject;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasServerException;
import com.baasbox.android.json.JsonObject;
import com.baasbox.android.net.HttpRequest;

import java.util.ArrayList;
import java.util.List;

public class BeerActivity extends AppCompatActivity  {

    private ListTask listTask;
    private AddTask addTask;
    private ArrayAdapter<BaasDocument> adapter;
    private ListView lvItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lvItem = (ListView)this.findViewById(R.id.lvBeer);
        adapter = new Adapter(this);
        lvItem.setAdapter(adapter);
        lvItem.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                BaasDocument item = (BaasDocument) lvItem.getItemAtPosition(position);
                addBeerToQueue(item);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
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

                MainActivity.Tag tag = new MainActivity.Tag();
                tag.text1 = (TextView) view.findViewById(android.R.id.text1);
                tag.text2 = (TextView) view.findViewById(android.R.id.text2);
                view.setTag(tag);
            }

            MainActivity.Tag tag = (MainActivity.Tag) view.getTag();
            BaasDocument entry = getItem(position);
            tag.text1.setText(entry.getString("name"));
            tag.text2.setText(entry.getString("price"));

            return view;
        }
    }

    private void refresh() {
        listTask = new ListTask();
        listTask.execute();
    }

    public class ListTask extends AsyncTask<Void, Void, BaasResult<List<BaasDocument>>> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected BaasResult<List<BaasDocument>> doInBackground(Void... params) {
            return BaasDocument.fetchAllSync("beers");
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

    protected void addBeerToQueue(final BaasDocument beer) {
        //addTask = new AddTask();
        //addTask.execute(beer.getString("name"), beer.getString("price"));
        BaasBox box = BaasBox.getDefault();

        // Plugin interface is easier called with the pass through API
        box.rest(HttpRequest.POST,
                "plugin/queueManagement.bb",
                new JsonObject().put("name", beer.getString("name")).put("price", beer.getString("price")),
                true,
                new BaasHandler<JsonObject>() {
                    @Override
                    public void handle(BaasResult<JsonObject> res) {
                        Log.d("TAG","Ok: " + res.isSuccess());
                        // the object comes back as plain json. Parsing necessary
                        BaasDocument beer = BaasDocument.from((JsonObject)res.value().get("data"));
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("item", beer);
                        setResult(RESULT_OK,returnIntent);
                        finish();
                    }
                });
    }

    public class AddTask extends AsyncTask<String, Void, BaasResult<BaasDocument>> {

        @Override
        protected BaasResult<BaasDocument> doInBackground(String... params) {
            BaasDocument person = new BaasDocument("queue");

            person.put("name", params[0]);
            person.put("price", params[1]);
            return person.saveSync();

        }

        @Override
        protected void onPostExecute(BaasResult<BaasDocument> result) {

            onPersonAdded(result);
        }
    }

    public void onPersonAdded(BaasResult<BaasDocument> result) {
        try {
            BaasDocument baas = result.get();
            Intent returnIntent = new Intent();
            returnIntent.putExtra("item", baas);
            setResult(RESULT_OK,returnIntent);
            finish();
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
