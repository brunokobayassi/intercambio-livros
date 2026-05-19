USE intercambio_livros;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS livros (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    usuario_id INT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS trocas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    livro_oferecido_id      INT NOT NULL,
    livro_recebido_id       INT NOT NULL,
    usuario_solicitante_id  INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDENTE',
    FOREIGN KEY (livro_oferecido_id)     REFERENCES livros(id),
    FOREIGN KEY (livro_recebido_id)      REFERENCES livros(id),
    FOREIGN KEY (usuario_solicitante_id) REFERENCES usuarios(id)
);
