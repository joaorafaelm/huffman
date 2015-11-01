 import Dependencias.*;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
/**
 * @aluno João Rafael Martins de Oliveira
 * @class Huffman
 *
 * Dependencias: 
 *      BinaryStdIn, BinaryStdOut
 *      MinPQ
 *
 * Referências:
 *       Data Compression [ONLINE] Available at: http://algs4.cs.princeton.edu/55compression/. [Accessed 19 October 2015].
 *    
 */
public class Huffman {

    /* Tamanho do alfabeto ASCII */
    private static final int alphabet = 256;
    
    /**
     * Chama os métodos de compactação ou descompactação,
     * dependendo dos argumentos usados:
     *
     * Usagem: Huffman -[zip | unzip] < source > target
     *
     * Opções:
     *      - para compactação
     *      + para descompactação
     * 
     * Tanto a leitura como a escrita dos arquivos, é realizado a partir do Standard stream | , < e >.
     * 
     * Exemplos:
     *      Compactação:
     *      java Huffman -zip < arquivo.txt > arquivo_compactado.txt
     *      cat arquivo.txt | java Huffman -zip > arquivo_compactado.txt
     *
     *      Descompactação:
     *      java Huffman -unzip < arquivo_compactado.txt > arquivo.txt
     *      cat arquivo_compactado.txt | java Huffman -unzip > arquivo.txt
     * 
     */
    public static void main(String[] args) {
            
        String usagem = "Usagem:\t java Huffman -[zip | unzip] < source > target\n"
                      + "\t Ex.: java Huffman -zip < arquivo.txt > compactado.txt\n"
                      + "\t Ex.: java Huffman -unzip < compactado.txt";
        
        if (args.length > 0) {

            if (args[0].equals("-zip") || args[0].equals("-unzip")) {
                
                if (args[0].equals("-zip")) 
                    compress();
                else 
                    expand();
                    
            }else{
                System.out.print(usagem); 
                System.exit(1);
            }

        }else{
            System.out.println(usagem); 
            System.exit(1);
        }
        
    }
    
    /* Árvore de Huffman */
    private static class Node implements Comparable<Node> {
        private final char ch;
        private final int freq;
        private final Node left, right;

        Node(char ch, int freq, Node left, Node right) {
            this.ch    = ch;
            this.freq  = freq;
            this.left  = left;
            this.right = right;
        }

        // is the node a leaf node?
        private boolean isLeaf() {
            assert ((left == null) && (right == null)) || ((left != null) && (right != null));
            return (left == null) && (right == null);
        }

        // compare, based on frequency
        public int compareTo(Node that) {
            return this.freq - that.freq;
        }
    }

    /**
     * Lê 1 byte do standard input, compacta a escreve no standard output;
     */
    public static void compress() {
        /* Standard input */
        String s = BinaryStdIn.readString();
        char[] input = s.toCharArray();

        /* Tabela de frequência */
        int[] freq = new int[alphabet];
        for (int i = 0; i < input.length; i++)
            freq[input[i]]++;

        /* Árvore de huffman */
        Node root = buildTrie(freq);

        /* Tabela de códigos */
        String[] st = new String[alphabet];
        buildCode(st, root, "");

        /* Escreve árvore no cabeçalho do arquivo */
        writeTrie(root);

        /* Número de bytes na mensagem inicial */
        BinaryStdOut.write(input.length);

        /* Usa tabela de códigos pra compactar byte e jogar no std out */
        for (int i = 0; i < input.length; i++) {
            String code = st[input[i]];
            for (int j = 0; j < code.length(); j++) {
                if (code.charAt(j) == '0') {
                    BinaryStdOut.write(false);
                }
                else if (code.charAt(j) == '1') {
                    BinaryStdOut.write(true);
                }
            }
        }

        // fecha output
        BinaryStdOut.close();
    }

    /**
    * Monta árvore de huffman de acordo com as frequências
    *
    * @param freq frequências dos chars.
    * @return Node
    */
    private static Node buildTrie(int[] freq) {

        // initialze priority queue with singleton trees
        MinPQ<Node> pq = new MinPQ<Node>();
        for (char i = 0; i < alphabet; i++)
            if (freq[i] > 0)
                pq.insert(new Node(i, freq[i], null, null));

        // special case in case there is only one character with a nonzero frequency
        if (pq.size() == 1) {
            if (freq['\0'] == 0) pq.insert(new Node('\0', 0, null, null));
            else                 pq.insert(new Node('\1', 0, null, null));
        }

        // merge two smallest trees
        while (pq.size() > 1) {
            Node left  = pq.delMin();
            Node right = pq.delMin();
            Node parent = new Node('\0', left.freq + right.freq, left, right);
            pq.insert(parent);
        }
        return pq.delMin();
    }

    /**
    * Escreve 1 byte compactado para o standard output
    */
    private static void writeTrie(Node x) {
        if (x.isLeaf()) {
            BinaryStdOut.write(true);
            BinaryStdOut.write(x.ch, 8);
            return;
        }
        BinaryStdOut.write(false);
        writeTrie(x.left);
        writeTrie(x.right);
    }

    /* Tabela de código */
    private static void buildCode(String[] st, Node x, String s) {
        if (!x.isLeaf()) {
            buildCode(st, x.left,  s + '0');
            buildCode(st, x.right, s + '1');
        }
        else {
            st[x.ch] = s;
        }
    }

    /**
     * Lê
     * Reads a sequence of bits that represents a Huffman-compressed message from
     * standard input; expands them; and writes the results to standard output.
     */
    public static void expand() {

        /* Lê arvore de frequencia do cabeçalho do arquivo */
        Node root = readTrie(); 

        /* Número de bytes do arquivo antes de compactar */
        int length = BinaryStdIn.readInt();

        /* Descompacta resto dos bytes usando root e length */
        for (int i = 0; i < length; i++) {
            Node x = root;
            while (!x.isLeaf()) {
                boolean bit = BinaryStdIn.readBoolean();
                if (bit) x = x.right;
                else     x = x.left;
            }
            BinaryStdOut.write(x.ch, 8);
        }
        BinaryStdOut.close();
    }


    private static Node readTrie() {
        boolean isLeaf = BinaryStdIn.readBoolean();
        if (isLeaf) {
            return new Node(BinaryStdIn.readChar(), -1, null, null);
        }
        else {
            return new Node('\0', -1, readTrie(), readTrie());
        }
    }

}
