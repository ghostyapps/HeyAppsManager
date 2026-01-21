package com.ghostyapps.heyappsmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView // Bunu ekle
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton // Material Button kullanıyoruz
import android.graphics.Color

class AppsAdapter(
    private val apps: List<AppModel>,
    private val onClick: (AppModel) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgIcon: ImageView = view.findViewById(R.id.imgAppIcon) // İkon Tanımı
        val txtName: TextView = view.findViewById(R.id.txtAppName)
        val txtDesc: TextView = view.findViewById(R.id.txtAppDesc)
        val btnAction: MaterialButton = view.findViewById(R.id.btnAction) // Tip değişti
        val lottieView: LottieAnimationView = view.findViewById(R.id.lottieLoading)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_card, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]

        holder.txtName.text = app.name
        holder.txtDesc.text = app.description

        // İKON SEÇİMİ (Manuel Eşleştirme)
        when (app.name) {
            "HeyCam" -> holder.imgIcon.setImageResource(R.drawable.ic_heycam_icon)
            "HeyNotes" -> holder.imgIcon.setImageResource(R.drawable.ic_heynotes_icon)
            "HeyBattery" -> holder.imgIcon.setImageResource(R.drawable.ic_heybattery_icon)
            "HeyWidgets" -> holder.imgIcon.setImageResource(R.drawable.ic_heywidgets_icon)
            "HeyApps" -> holder.imgIcon.setImageResource(R.drawable.ic_heyapps_icon)
            else -> holder.imgIcon.setImageResource(android.R.drawable.sym_def_app_icon)
        }

        // RESETLEME
        holder.btnAction.isEnabled = true
        holder.btnAction.alpha = 1.0f

        // BUTON RENKLERİNİ DURUMA GÖRE AYARLAMAK İSTERSEN:
        // holder.btnAction.setBackgroundColor(Color.BLUE) vb.

// ... (önceki kodlar)

        // Butonun rengini duruma göre değiştiriyoruz
        when (app.status) {
            AppStatus.DOWNLOADING -> {
                holder.btnAction.visibility = View.INVISIBLE
                holder.lottieView.visibility = View.VISIBLE
                if (!holder.lottieView.isAnimating) holder.lottieView.playAnimation()
            }
            AppStatus.LOADING -> {
                holder.lottieView.visibility = View.GONE
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.text = "Checking"
                holder.btnAction.isEnabled = false
                holder.btnAction.alpha = 0.5f
                // Loading rengi (Gri)
                holder.btnAction.setBackgroundColor(Color.parseColor("#9E9E9E"))
            }
            AppStatus.INSTALLED -> {
                holder.lottieView.visibility = View.GONE
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.text = "Open"

                // YÜKLÜYSE YEŞİL OLSUN (Veya istediğin renk)
                holder.btnAction.setBackgroundColor(Color.parseColor("#4CAF50"))
            }
            AppStatus.UPDATE_AVAILABLE -> {
                holder.lottieView.visibility = View.GONE
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.text = "Update"

                // GÜNCELLEME VARSA TURUNCU/MAVİ OLSUN
                holder.btnAction.setBackgroundColor(Color.parseColor("#FF6F00"))
            }
            AppStatus.NOT_INSTALLED -> {
                holder.lottieView.visibility = View.GONE
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.text = "Download"

                // İNDİRMEK İÇİN ANA RENK (Mor/Mavi)
                holder.btnAction.setBackgroundColor(Color.parseColor("#333333"))
            }
        }
        holder.btnAction.setOnClickListener {
            onClick(app)
        }
    }

    override fun getItemCount() = apps.size
}