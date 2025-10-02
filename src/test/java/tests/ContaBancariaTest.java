package tests;

import model.ContaBancaria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes da Classe ContaBancaria")
class ContaBancariaTest {

    @Test
    @DisplayName("Deve lançar exceção ao criar conta com saldo inicial negativo")
    void construtor_SaldoInicialNegativo_LancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ContaBancaria("Titular Inválido", -100.0);
        }, "Deve lançar IllegalArgumentException para saldo inicial negativo.");
    }

    @Nested
    @DisplayName("Operações em uma Conta Existente")
    class OperacoesEmConta {

        private ContaBancaria conta;

        @BeforeEach
        void setUp() {
            // Saldo inicial generoso para cobrir vários cenários de saque
            conta = new ContaBancaria("João da Silva", 1500.0);
        }

        @Test
        @DisplayName("Deve depositar valor válido")
        void depositar_ValorValido_RetornaTrueEAlteraSaldo() {
            assertTrue(conta.depositar(100.0));
            assertEquals(1600.0, conta.getSaldo());
        }

        @Test
        @DisplayName("Não deve depositar valor negativo ou zero")
        void depositar_ValorInvalido_RetornaFalse() {
            assertFalse(conta.depositar(-50.0));
            assertFalse(conta.depositar(0));
            assertEquals(1500.0, conta.getSaldo(), "Saldo não deve mudar para depósitos inválidos.");
        }

        @Test
        @DisplayName("Deve sacar valor com saldo e limite diário suficientes")
        void sacar_ValorValido_RetornaTrueEAlteraSaldo() {
            assertTrue(conta.sacar(500.0));
            assertEquals(1000.0, conta.getSaldo());
        }

        @Test
        @DisplayName("Não deve sacar valor com saldo insuficiente")
        void sacar_SaldoInsuficiente_RetornaFalse() {
            assertFalse(conta.sacar(2000.0));
            assertEquals(1500.0, conta.getSaldo(), "Saldo não deve mudar para saque com saldo insuficiente.");
        }

        @Test
        @DisplayName("Não deve sacar valor acima do limite diário")
        void sacar_AcimaLimiteDiario_RetornaFalse() {
            assertTrue(conta.sacar(600.0)); // Saque 1
            assertEquals(900.0, conta.getSaldo());
            assertTrue(conta.sacar(400.0)); // Saque 2 (total sacado hoje = 1000)
            assertEquals(500.0, conta.getSaldo());
            
            // Tenta sacar mais 1.0, ultrapassando o limite de 1000.0
            assertFalse(conta.sacar(1.0)); 
            assertEquals(500.0, conta.getSaldo(), "Saldo não deve mudar ao exceder o limite diário.");
        }
        
        @Test
        @DisplayName("Deve resetar o limite de saque diário")
        void resetarSaqueDiario_ZeraTotalSacado() {
            conta.sacar(500.0);
            conta.resetarSaqueDiario();
            // Após resetar, deve ser possível sacar o valor total novamente
            assertTrue(conta.sacar(1000.0));
            assertEquals(0.0, conta.getSaldo());
        }

        @Test
        @DisplayName("Não deve sacar valor negativo ou zero")
        void sacar_ValorInvalido_RetornaFalse() {
            assertFalse(conta.sacar(-100.0));
            assertFalse(conta.sacar(0));
            assertEquals(1500.0, conta.getSaldo(), "Saldo não deve mudar para saques inválidos.");
        }

        @Test
        @DisplayName("Deve encerrar a conta se o saldo for zero")
        void encerrarConta_ComSaldoZero_RetornaTrueEInativaConta() {
            conta.sacar(1500.0); // Zera o saldo
            assertTrue(conta.encerrarConta());
            assertFalse(conta.isAtiva());
        }

        @Test
        @DisplayName("Não deve encerrar a conta se houver saldo")
        void encerrarConta_ComSaldoPositivo_RetornaFalse() {
            assertFalse(conta.encerrarConta());
            assertTrue(conta.isAtiva());
        }

        @Test
        @DisplayName("Não deve permitir operações em conta encerrada")
        void operacoes_EmContaEncerrada_RetornaFalse() {
            conta.sacar(1500.0);
            conta.encerrarConta(); // Conta agora está inativa

            assertFalse(conta.depositar(100.0));
            assertFalse(conta.sacar(1.0));
            assertEquals(0.0, conta.getSaldo(), "Saldo não deve mudar em conta inativa.");
        }

        @Nested
        @DisplayName("Testes de Transferência")
        class Transferencia {
            private ContaBancaria contaDestino;

            @BeforeEach
            void setUp() {
                contaDestino = new ContaBancaria("Maria Souza", 200.0);
            }

            @Test
            @DisplayName("Deve transferir com sucesso entre contas ativas com saldo")
            void transferir_ValoresValidos_RetornaTrue() {
                assertTrue(conta.transferir(contaDestino, 300.0));
                assertEquals(1200.0, conta.getSaldo());
                assertEquals(500.0, contaDestino.getSaldo());
            }

            @Test
            @DisplayName("Não deve transferir de conta com saldo insuficiente")
            void transferir_SaldoInsuficiente_RetornaFalse() {
                assertFalse(conta.transferir(contaDestino, 2000.0));
                assertEquals(1500.0, conta.getSaldo());
                assertEquals(200.0, contaDestino.getSaldo());
            }

            @Test
            @DisplayName("Não deve transferir para conta inativa")
            void transferir_ParaContaInativa_RetornaFalse() {
                contaDestino.sacar(200.0);
                contaDestino.encerrarConta(); // Destino inativo

                assertFalse(conta.transferir(contaDestino, 100.0));
                assertEquals(1500.0, conta.getSaldo(), "Saldo da origem não deve mudar.");
                assertEquals(0.0, contaDestino.getSaldo(), "Saldo do destino inativo não deve mudar.");
            }
            
            @Test
            @DisplayName("Não deve transferir valor que excede limite diário da conta de origem")
            void transferir_ExcedeLimiteDiario_RetornaFalse() {
                // Saca 900, restam 100 de limite de saque diário
                conta.sacar(900.0);
                
                // Tenta transferir 200. A transferência usa o 'sacar', então vai falhar pelo limite.
                assertFalse(conta.transferir(contaDestino, 200.0));
                
                assertEquals(600.0, conta.getSaldo(), "Saldo da origem não deve mudar após transferência falha.");
                assertEquals(200.0, contaDestino.getSaldo(), "Saldo do destino não deve mudar.");
            }
        }
    }
}