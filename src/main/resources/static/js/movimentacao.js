(function () {
    var produto = document.getElementById('produtoId');
    var info = document.getElementById('saldoInfo');
    var rotulo = document.getElementById('rotuloQtd');
    var tipos = document.querySelectorAll('input[name="tipo"]');

    function atualizar() {
        var opcao = produto.options[produto.selectedIndex];
        var temSaldo = opcao && opcao.getAttribute('data-saldo') !== null;
        info.textContent = temSaldo
            ? 'Saldo atual: ' + opcao.getAttribute('data-saldo') + ' ' + opcao.getAttribute('data-un')
            : '';

        var marcado = document.querySelector('input[name="tipo"]:checked');
        if (marcado) {
            rotulo.textContent = marcado.getAttribute('data-rotulo');
        }
    }

    produto.addEventListener('change', atualizar);
    for (var i = 0; i < tipos.length; i++) {
        tipos[i].addEventListener('change', atualizar);
    }
    atualizar();
})();
