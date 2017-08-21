package studio.papercube.ngdownloader.fragments

import android.annotation.SuppressLint
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
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
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        container!!
        val view: View = LayoutInflater.from(container.context).inflate(R.layout.fragment_downloaded_songs_manager, container, false)
        recyclerView = view.findViewById(R.id.layout_recycler_view_downloaded_songs) as RecyclerView
        swipeRefreshLayout = view.findViewById(R.id.layout_swipe_refresh_in_downloaded_song_manager) as SwipeRefreshLayout
        initView()
        return view
    }

    private fun initView() {
        val activity = this.activity ?: let {
            Log.w(LOG_TAG_IGNORED_EXCEPTION, "Cannot init view, because fragment is detached from activity")
            return
        }
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerViewAdapter = SongRecyclerViewAdapter(activity)
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.itemAnimator = DefaultItemAnimator()

        swipeRefreshLayout.setOnRefreshListener {
            recyclerViewAdapter.refresh(swipeRefreshLayout)
        }
    }

    protected open class SongRecyclerViewAdapter(private val ctx: Context)
        : RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder>() {

        private val localSongList: ArrayList<LocalSong> = ArrayList()

        init {
            refresh(null)
        }

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
            holder ?: return
            with(holder) {
                //require non-null
                val song = localSongList[position]
                val songSize = song.size.toAppropriateMemoryUnit()

                boundLocalSong = song

                textViewNameAndId.text = "${song.name} (${song.id})"
                textViewInfo.text = StringBuilder()
                        .append(ctx.getText(R.string.song_param_artist))
                        .appendln(" : ${song.artist}")
                        .appendln(song.fileName)
                        .append(songSize)
                        .toString()

                button.setOnClickListener {
                    val popupMenu = PopupMenu(ctx, button)
                    popupMenu.menuInflater.inflate(R.menu.popup_downloaded_song_options, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_popup_delete_downloaded_song -> {
                                boundLocalSong?.delete()
                                this@SongRecyclerViewAdapter.remove(boundLocalSong)
                            }
                            else -> return@setOnMenuItemClickListener false
                        }
                        true
                    }
                    popupMenu.show()
                }
            }
        }

        fun remove(song: LocalSong?) {
            song ?: return
            val index = localSongList.indexOf(song)
            localSongList.removeAt(index)
            notifyItemRemoved(index)
        }

        override fun getItemCount(): Int {
            return localSongList.size
        }

        open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var boundLocalSong: LocalSong? = null
            val textViewNameAndId: TextView = itemView.findViewById(R.id.text_view_downloaded_song_name_id) as TextView
            val textViewInfo: TextView = itemView.findViewById(R.id.text_view_downloaded_song_info) as TextView
            val button: ImageButton = itemView.findViewById(R.id.button_downloaded_song_options) as ImageButton
        }

        fun refresh(refreshLayout: SwipeRefreshLayout?) {
            localSongList.clear()
            localSongList.addAll(LocalSong.Loader.loadSongs(ctx))
            notifyDataSetChanged()
            refreshLayout?.isRefreshing = false
        }
    }


}