package studio.papercube.ngdownloader.fragments

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import studio.papercube.ngdownloader.R

class DownloadedSongsManagerFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater?.inflate(R.layout.fragment_downloaded_songs_manager, container, false) ?:
                super.onCreateView(inflater, container, savedInstanceState)
    }
}