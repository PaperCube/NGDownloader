package studio.papercube.ngdownloader.fragments

import android.annotation.SuppressLint
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import studio.papercube.ngdownloader.LOG_TAG_IGNORED_EXCEPTION
import studio.papercube.ngdownloader.LocalSong
import studio.papercube.ngdownloader.MemoryUnitConversions.toAppropriateMemoryUnit
import studio.papercube.ngdownloader.R

open class DownloadedSongsManagerFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: SongRecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        container!!
        val view: View = LayoutInflater.from(container.context).inflate(R.layout.fragment_downloaded_songs_manager, container, false)
        recyclerView = view.findViewById(R.id.layout_recycler_view_downloaded_songs) as RecyclerView
        initView()
        return view
    }

    private fun initView() {
        val activity = this.activity ?: let {
            Log.w(LOG_TAG_IGNORED_EXCEPTION, "Cannot init view, because fragment is detached from activity")
            return
        }
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerViewAdapter = SongRecyclerViewAdapter(activity, LocalSong.Loader.loadSongs(activity))
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    protected open class SongRecyclerViewAdapter(private val ctx: Context, data: List<LocalSong>)
        : RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder>() {

        private val dataList: ArrayList<LocalSong> = ArrayList(data)

        /**
         * Play an role as ViewHolderSupplier.
         */
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SongRecyclerViewAdapter.ViewHolder {
            parent!! //check if parent is null. If null, throw an exception.
            val view = LayoutInflater.from(parent.context).inflate(R.layout.view_downloaded_song, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: SongRecyclerViewAdapter.ViewHolder?, position: Int) {
            with(holder!!) {
                //require non-null
                val song = dataList[position]
                val songSize = song.size.toAppropriateMemoryUnit()

                textViewNameAndId.text = "${song.name} (${song.id})"
                textViewInfo.text = StringBuilder()
                        .append(ctx.getText(R.string.song_param_artist))
                        .appendln(" : ${song.artist}")
                        .appendln(song.fileName)
                        .append(songSize)
                        .toString()
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textViewNameAndId: TextView = itemView.findViewById(R.id.text_view_downloaded_song_name_id) as TextView
            val textViewInfo: TextView = itemView.findViewById(R.id.text_view_downloaded_song_info) as TextView
            val button: ImageButton = itemView.findViewById(R.id.button_downloaded_song_options) as ImageButton
        }
    }


}