package model;
public class ContaBancaria {
private String titular;
private double saldo;
private boolean ativa;
private double limiteDiario = 1000.0;
private double totalSacadoHoje = 0.0;
public ContaBancaria(String titular, double saldoInicial) {
if (saldoInicial < 0) {
throw new IllegalArgumentException("Saldo inicial não pode ser negativo.");
}
this.titular = titular;
this.saldo = saldoInicial;
this.ativa = true;
}
public String getTitular() {
return titular;
}
public double getSaldo() {
return saldo;
}
public boolean isAtiva() {
return ativa;
}
public boolean depositar(double valor) {
if (!ativa || valor <= 0) {
return false;
}
saldo += valor;
return true;
}
public boolean sacar(double valor) {
if (!ativa || valor <= 0) {
return false;
}
if (valor > saldo) {
return false;
}
if (totalSacadoHoje + valor > limiteDiario) {
return false;
}
saldo -= valor;
totalSacadoHoje += valor;
return true;
}
public boolean transferir(ContaBancaria destino, double valor) {
if (!ativa || !destino.isAtiva() || valor <= 0) {
return false;
}
// Esta lógica de saque já verifica saldo e limite diário
if (this.sacar(valor)) {
    if (destino.depositar(valor)) {
        return true;
    } else {
        // Se o depósito falhar no destino, o valor deve ser estornado para a origem.
        this.depositar(valor); 
        return false;
    }
}
return false;
}
public boolean encerrarConta() {
if (saldo == 0) {
ativa = false;
return true;
}
return false;
}
// Método auxiliar para simular "resetar" o saque diário (como se fosse outro dia)
public void resetarSaqueDiario() {
totalSacadoHoje = 0.0;
}
}