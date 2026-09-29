override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_wear)

    // Inicializar diálogo de modo en el reloj
    val layoutMode = findViewById<View>(R.id.layoutWearModeSelection)
    val btnNormal = findViewById<Button>(R.id.btnWearModeNormal)
    val btnGold = findViewById<Button>(R.id.btnWearModeGold)

    btnNormal?.setOnClickListener {
        isGoldenPoint = false
        layoutMode?.visibility = View.GONE
    }

    btnGold?.setOnClickListener {
        isGoldenPoint = true
        layoutMode?.visibility = View.GONE
    }
}
