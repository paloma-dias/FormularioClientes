package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormularioClientes extends JFrame {
    private JTextField txtNome, txtEmail;
    private JTextArea areaClientes;
    //private List<Cliente> clientes = new ArrayList<>();
    private Connection connection;
    private JButton btnDeletar;

    public FormularioClientes() {
        setTitle("Cadastro de Clientes");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 400);
        setLayout(new BorderLayout());

        // Inicializando o banco de dados
        try {
            inicializarBancoDados();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao inicializar o banco de dados: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Painel do formulário
        JPanel painelForm = new JPanel(new GridLayout(3, 2));
        painelForm.add(new JLabel("Nome:"));
        txtNome = new JTextField();
        painelForm.add(txtNome);
        painelForm.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        painelForm.add(txtEmail);

        JButton btnSalvar = new JButton("Salvar");
        painelForm.add(btnSalvar);

        JPanel painelBotoes = new JPanel(new FlowLayout());
        btnDeletar = new JButton("Deletar Selecionado");
        btnDeletar.setEnabled(false);
        painelBotoes.add(btnDeletar);

        add(painelForm, BorderLayout.NORTH);
        add(painelBotoes, BorderLayout.SOUTH);

        // Área para exibir clientes
        areaClientes = new JTextArea();
        areaClientes.setEditable(false);
        // Selecionar o texto para habilitar o delete
        areaClientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                habilitarDelete();
            }
        });
        add(new JScrollPane(areaClientes), BorderLayout.CENTER);

        // Ação do botão
        btnSalvar.addActionListener(e -> salvarCliente());
        btnDeletar.addActionListener(e -> deletarCliente());

        carregarClientesBanco();
        setVisible(true);
    }

    private void habilitarDelete() {
        btnDeletar.setEnabled(areaClientes.getSelectedText() != null);
    }

    private void deletarCliente() {
        try{
            String texto = areaClientes.getSelectedText();
            if (texto == null) return;

            String email = texto.split("\\(")[1].replace(")", "").trim();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Funcionaaaaa pelo amor de deussss", "AAAAAAAAAAA",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM clientes WHERE email = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, email);
                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "O cliente FINALMENTE FOI DELETADO NESSA MERDAAAA!");
                        carregarClientesBanco();
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao deletar: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um cliente na lista",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void inicializarBancoDados() {
        try {
            // Carrega o driver JDBC
            Class.forName("org.sqlite.JDBC");

            // Estabelece a conexão (cria o banco se não existir)
            connection = DriverManager.getConnection("jdbc:sqlite:clientes.db");

            // Cria a tabela se não existir
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS clientes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL, " +
                    "email TEXT NOT NULL UNIQUE)");
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao conectar ao banco de dados: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvarCliente() {
        try {
            String nome = txtNome.getText().trim();
            String email = txtEmail.getText().trim();

            if (nome.isEmpty() || email.isEmpty()) {
                throw new IllegalArgumentException("Preencha todos os campos.");
            }

            salvarClienteNoBanco(nome, email);
            carregarClientesBanco();
            limparCampos();

        }catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            if (ex.getMessage().contains("UNIQUE constraint failed")) {
                JOptionPane.showMessageDialog(this,
                        "Email já cadastrado",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Erro: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvarClienteNoBanco(String nome, String email) throws SQLException {
        String sql = "INSERT INTO clientes (nome, email) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "as vezes p sucesso e nem tentar!");
        }
    }

    private void carregarClientesBanco() {
        try {
            //clientes.clear();
            StringBuilder sb = new StringBuilder();
            String sql = "SELECT nome, email FROM clientes";

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String nome = rs.getString("nome");
                    String email = rs.getString("email");
                    //clientes.add(new Cliente(nome, email));
                    sb.append(nome).append(" (").append(email).append(")\n");
                }
            }
            //atualizarLista();

            areaClientes.setText(sb.toString());
            btnDeletar.setEnabled(false);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /*private void atualizarLista() {
        StringBuilder sb = new StringBuilder();
        for (Cliente c : clientes) {
            sb.append(c).append("\n");
        }
        areaClientes.setText(sb.toString());
        btnDeletar.setEnabled(false);
    }*/

    private void limparCampos() {
        txtNome.setText("");
        txtEmail.setText("");
        txtNome.requestFocus();
    }

    @Override
    public void dispose() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FormularioClientes::new);
    }
}


