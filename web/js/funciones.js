function resultado_upload(mensaje) {
    var contenedor	= window.parent.document.getElementById('formulario_nuevo'); 
    var html	= "";    
    html = mensaje;    
    contenedor.innerHTML = html;
}


