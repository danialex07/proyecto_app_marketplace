package com.daniel.appcomprayventa.Anuncios

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.daniel.appcomprayventa.Adaptadores.AdaptadorImagenSeleccionada
import com.daniel.appcomprayventa.Constantes
import com.daniel.appcomprayventa.MainActivity
import com.daniel.appcomprayventa.Modelo.ModeloImageSeleccionada
import com.daniel.appcomprayventa.R
import com.daniel.appcomprayventa.SeleccionarUbicacion
import com.daniel.appcomprayventa.databinding.ActivityCrearAnuncioBinding
import com.daniel.appcomprayventa.databinding.ItemImagenesSeleccionadasBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class CrearAnuncio : AppCompatActivity() {
    private lateinit var  binding: ActivityCrearAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var imagenUri : Uri?=null

    private  lateinit var imagenSelecArrayList: ArrayList<ModeloImageSeleccionada>
    private lateinit var  adaptadorImagenSel : AdaptadorImagenSeleccionada


    private var Edicion = false
    private var idAnuncioEditar = ""






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCrearAnuncioBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)

        progressDialog.setTitle("espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        val adaptadorCat = ArrayAdapter(this,R.layout.item_categoria,Constantes.categorias)
        binding.Categoria.setAdapter(adaptadorCat)

        val  adaptadorCon = ArrayAdapter(this,R.layout.item_condicion, Constantes.condiciones)
        binding.Condicion.setAdapter(adaptadorCon)

        Edicion = intent.getBooleanExtra("Edicion",false)

        /*
        identificamos  de que activity estamos llegando

         */

        if (Edicion){
            //true
            //llegamos de la actividad detalle anuncio
            idAnuncioEditar =intent.getStringExtra("idAnuncio")?: ""
            cargarDetalles()
            binding.BtnCrearAnuncio.text = "Actualizar anuncio"
        }else{
            //False
            //LLegando de la actividad Main activity
            binding.BtnCrearAnuncio.text = "Crear anuncio"

        }

        imagenSelecArrayList = ArrayList()
        cargarImagenes()

        binding.agregarImg.setOnClickListener {
            mostrarOpciones()

        }

        binding.Locacion.setOnDismissListener {
            val intent =Intent(this, SeleccionarUbicacion::class.java)
            //startActivity(Intent(this,SeleccionarUbicacion::class.java))
            seleccionarUbicacion_ARL.launch(intent)
        }
        binding.BtnCrearAnuncio.setOnClickListener {
            validarDatos()
        }


    }

    private fun cargarDetalles() {
        var ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncioEditar)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    /*
                    obtener de la base de dato la informacion del anuncio
                     */
                    val marca = "${snapshot.child("marca").value}"
                    val categoria = "${snapshot.child("categoria").value}"
                    val condicion = "${snapshot.child("condicion").value}"
                    val locacion = "${snapshot.child("direccion").value}"
                    val precio = "${snapshot.child("precio").value}"
                    val titulo = "${snapshot.child("titulo").value}"
                    val descripcion = "${snapshot.child("descripcion").value}"
                    latitud = (snapshot.child("latitud").value) as Double
                    longitud = (snapshot.child("longitud").value) as Double

                    /*
                    seteamos  la informacion en lAS Vistas
                     */
                    binding.EtMarca.setText(marca)
                    binding.Categoria.setText(categoria)
                    binding.Categoria.isEnabled = false
                    binding.Condicion.setText(condicion)
                    binding.Condicion.isEnabled = false
                    binding.Locacion.setText(locacion)
                    binding.EtPrecio.setText(precio)
                    binding.EtTitulo.setText(titulo)
                    binding.EtDescripcion.setText(descripcion)


                    val refImagenes = snapshot.child("Imagenes").ref
                    refImagenes.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children){
                                val id =  "${ds.child("id").value}"
                                val imagenUrl = "${ds.child("imagenUrl").value}"

                                val modeloImagenSeleccionada = ModeloImageSeleccionada(id,null,imagenUrl,true)
                                imagenSelecArrayList.add(modeloImagenSeleccionada)

                            }
                            cargarImagenes()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private var marca =""
    private var categoria = ""
    private var condicion =""
    private var direccion = ""
    private var precio =""
    private var titulo = ""
    private var descripcion =""
    private var latitud = 0.0
    private var longitud =0.0
    private fun validarDatos(){
        marca = binding.EtMarca.text.toString().trim()
        categoria = binding.Categoria.text.toString().trim()
        condicion = binding.Condicion.text.toString().trim()
        direccion = binding.Locacion.text.toString().trim()
        precio = binding.EtPrecio.text.toString().trim()
        titulo = binding.EtTitulo.text.toString().trim()
        descripcion = binding.EtDescripcion.text.toString().trim()

        if (marca.isEmpty()){
            binding.EtMarca.error = "Ingrese una marca"
            binding.EtMarca.requestFocus()
        }
        else if(categoria.isEmpty()){
            binding.Categoria.error = "Ingrese una Categoria"
            binding.Categoria.requestFocus()


        }
        else if(condicion.isEmpty()){
            binding.Condicion.error = "Ingrese una Condicion"
            binding.Condicion.requestFocus()

        }
        else if(direccion.isEmpty()){
            binding.Locacion.error = "Ingrese una locacion"
            binding.Locacion.requestFocus()

        }
        else if(precio.isEmpty()){
            binding.EtPrecio.error = "Ingrese una Precio"
            binding.EtPrecio.requestFocus()

        }
        else if(titulo.isEmpty()){
            binding.EtTitulo.error = "Ingrese una Titulo"
            binding.EtTitulo.requestFocus()

        }
        else if(descripcion.isEmpty()){
            binding.EtDescripcion.error = "Ingrese una Descripcion"
            binding.EtDescripcion.requestFocus()

        }
        else{
            if (Edicion){
                actualizarAnuncio()
            }else{
                if (imagenUri == null){
                    Toast.makeText(this, "agrege al menos una imagen",Toast.LENGTH_SHORT).show()

                }else{
                    agregarAnuncio()
                }


            }

        }


    }

    private fun actualizarAnuncio() {
        progressDialog.setMessage("Actualizando anuncio")
        progressDialog.show()


        val hashMap = HashMap<String, Any>()

        hashMap["marca"] ="${marca}"
        hashMap["categoria"] ="${categoria}"
        hashMap["condicion"] ="${condicion}"
        hashMap["direccion"] ="${direccion}"
        hashMap["precio"]="${precio}"
        hashMap["titulo"] ="${titulo}"
        hashMap["descripcion"] = "${descripcion}"
        hashMap["latitud"] =latitud
        hashMap["longitud"]=longitud


        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncioEditar)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                cargarImagenesStorage(idAnuncioEditar)

            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Fallo la Actualizacion debido a ${e.message}",Toast.LENGTH_SHORT).show()

            }


    }

    private val seleccionarUbicacion_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if(resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                if (data != null){
                    latitud = data.getDoubleExtra("latitud",0.0)
                    longitud = data.getDoubleExtra("longitud",0.0)
                    direccion = data.getStringExtra("direccion")?:""


                    binding.Locacion.setText(direccion)
                }

            }else{
                Toast.makeText(this,"cancelado",Toast.LENGTH_SHORT).show()

            }

        }

    private fun agregarAnuncio() {
        progressDialog.setMessage("Agregando anuncio")
        progressDialog.show()


        val tiempo = Constantes.obtenerTiempoDis()
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")

        val keyId = ref.push().key


        val hashMap = HashMap<String, Any>()
        hashMap["id"]="${keyId}"
        hashMap["uid"]="${firebaseAuth.uid}"
        hashMap["marca"] ="${marca}"
        hashMap["categoria"] ="${categoria}"
        hashMap["condicion"] ="${condicion}"
        hashMap["direccion"] ="${direccion}"
        hashMap["precio"]="${precio}"
        hashMap["titulo"] ="${titulo}"
        hashMap["descripcion"] = "${descripcion}"
        hashMap["estado"] ="${Constantes.anuncio_disponible}"
        hashMap["tiempo"]=tiempo
        hashMap["latitud"] =latitud
        hashMap["longitud"]=longitud



        ref.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                cargarImagenesStorage(keyId)

            }
            .addOnFailureListener {e->
                Toast.makeText(
                    this, "${e.message}",Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun cargarImagenesStorage(keyId: String) {
        for (i in imagenSelecArrayList.indices){
            val modeloImagenSel = imagenSelecArrayList[i]
            if (!modeloImagenSel.deInternet){
                val  nombreImagen = modeloImagenSel.id
                val rutaNombreImagen = "Anuncios/$nombreImagen"

                val storageReference = FirebaseStorage.getInstance().getReference(rutaNombreImagen)
                storageReference.putFile(modeloImagenSel.imagenUri!!)
                    .addOnSuccessListener {taskSnaphot->
                        val uriTask = taskSnaphot.storage.downloadUrl
                        while (!uriTask.isSuccessful);
                        val urlImgCargada = uriTask.result

                        if (uriTask.isSuccessful){
                            val hashMap = HashMap<String, Any>()
                            hashMap["id"] = "${modeloImagenSel.id}"
                            hashMap["imagenUrl"] ="$urlImgCargada"


                            val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
                            ref.child(keyId).child("Imagenes")
                                .child(nombreImagen)
                                .updateChildren(hashMap)
                        }

                        if (Edicion){

                            progressDialog.dismiss()
                            val intent = Intent(this@CrearAnuncio, MainActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Se actualizara la informacion del anuncio", Toast.LENGTH_SHORT).show()
                            finishAffinity()
                        }else{
                            progressDialog.dismiss()

                            Toast.makeText(this,
                                "se publico su anuncio",
                                Toast.LENGTH_SHORT
                            ).show()
                            limpiarCampos()

                        }



                    }
                    .addOnFailureListener {e->
                        Toast.makeText(
                            this, "${e.message}",Toast.LENGTH_SHORT
                        ).show()

                    }

            }




        }

    }
    private fun limpiarCampos(){
        imagenSelecArrayList.clear()
        adaptadorImagenSel.notifyDataSetChanged()
        binding.EtMarca.setText("")
        binding.Categoria.setText("")
        binding.Condicion.setText("")
        binding.Locacion.setText("")
        binding.EtPrecio.setText("")
        binding.EtTitulo.setText("")
        binding.EtDescripcion.setText("")
    }




    private fun mostrarOpciones() {
        val popuMenu = PopupMenu(this,binding.agregarImg)
        popuMenu.menu.add(Menu.NONE,1,1,"Camara")
        popuMenu.menu.add(Menu.NONE,2,2,"Galeria")

        popuMenu.show()

        popuMenu.setOnMenuItemClickListener {item->
            val itemId = item.itemId
            if(itemId ==1){
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
                    solicitarPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA))

                }else{
                    solicitarPermisoCamara.launch(arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE


                    ))
                }

            }else if(itemId == 2){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    imagenGaleria()
                }else{
                    solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }

            }
            true

        }
    }

    private  val solicitarPermisoAlmacenamiento = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){esConcedido->
        if(esConcedido){
            imagenGaleria()

        }else{
            Toast.makeText(
                this,
                "El permiso de almacenamiento  ha sido denegada",
                Toast.LENGTH_SHORT

            ).show()

        }

    }

    private fun imagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type ="image/*"
        resultadoGaleria_ARL.launch(intent)
    }
    private val resultadoGaleria_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                imagenUri = data!!.data
                val tiempo = "${Constantes.obtenerTiempoDis()}"
                val modeloImgSel = ModeloImageSeleccionada(
                    tiempo, imagenUri, null,false
                )
                imagenSelecArrayList.add(modeloImgSel)
                cargarImagenes()

            }else{
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()


            }

        }






    private val solicitarPermisoCamara = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){resultado->
        var todosConcedidos = true
        for (esConcedido in  resultado.values){
            todosConcedidos =  todosConcedidos &&  esConcedido
        }
        if(todosConcedidos){
            imagenCamara()

        }else{
            Toast.makeText(
                this,
                "El permiso de la camara o almacenamiento ha sido denegada",
                Toast.LENGTH_SHORT

            ).show()
        }

    }


    private fun imagenCamara() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Titulo_imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Descripcion_imagen")
        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)


        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        resultadoCamara_ARL.launch(intent)


    }


    private val resultadoCamara_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if(resultado.resultCode == Activity.RESULT_OK){
                val  tiempo  = "${Constantes.obtenerTiempoDis()}"
                val modeloImgSel = ModeloImageSeleccionada(
                    tiempo, imagenUri, null, false
                )
                imagenSelecArrayList.add(modeloImgSel)
                cargarImagenes()



            }else{
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }








    private fun cargarImagenes() {
        adaptadorImagenSel = AdaptadorImagenSeleccionada(this,imagenSelecArrayList, idAnuncioEditar)
        binding.RVImagenes.adapter = adaptadorImagenSel
    }
}