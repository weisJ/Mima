package edu.kit.mima.formatter.syntaxtree;

import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.token.SyntaxToken;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Syntax tree for Syntax Tokens.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class SyntaxTree {

    @NotNull private final SyntaxToken[] tokens;
    @NotNull private final SyntaxNode root;

    /**
     * Create syntax tree.
     *
     * @param tokens tokens to build tree from
     */
    public SyntaxTree(@NotNull final SyntaxToken[] tokens) {
        this.tokens = tokens;
        this.root = new SimpleSyntaxNode(NodeType.ROOT, 0, tokens.length, null);
        build();
    }

    /**
     * Get the root node.
     *
     * @return root node
     */
    @NotNull
    public SyntaxNode root() {
        return root;
    }

    /**
     * Build the tree.
     */
    private void build() {
        buildGroundLayer();
        buildScopes();
    }

    /**
     * Build ground layer consisting of leaf nodes.
     */
    private void buildGroundLayer() {
        for (int i = 0; i < tokens.length; i++) {
            root.addChild(tokenToNode(tokens[i], i));
        }
    }

    /**
     * Build scopes.
     */
    private void buildScopes() {
        final Stack<SyntaxNode> scopes = new Stack<>();
        scopes.add(root);
        for (final var n : new ArrayList<>(root.children())) {
            if (n.getType() == NodeType.SCOPE_OPEN) {
                scopes.peek().addChild(n);
                scopes.add(new SimpleSyntaxNode(NodeType.SCOPE, n.getBegin(), -1, null));
            } else if (n.getType() == NodeType.SCOPE_CLOSED && !scopes.isEmpty()) {
                final SyntaxNode scope = scopes.pop();
                scopes.peek().addChild(scope);
                scopes.peek().addChild(n);
                buildLayers(scope);
            } else if (!scopes.isEmpty()) {
                scopes.peek().addChild(n);
            }
        }
        buildLayers(root);
    }

    private void buildLayers(@NotNull final SyntaxNode node) {
        buildLines(node);
        groupAndCreate(node, NodeType.JUMP_DEL, NodeType.JUMP);
        groupAndCreate(node, NodeType.INSTRUCTION_END, NodeType.INSTRUCTION);
        buildBlocks(node);
    }

    /**
     * Group lines.
     *
     * @param node parent node to create lines on
     */
    private void buildLines(@NotNull final SyntaxNode node) {
        insertLayer(n -> n.getType() == NodeType.NEW_LINE,
                    (x, y) -> new SimpleSyntaxNode(NodeType.LINE, x, y, null), node);
    }

    /**
     * Split layer at node type and create new node from split.
     *
     * @param node       Node to perform on.
     * @param split      type to split at.
     * @param createType type of new nodes.
     */
    private void groupAndCreate(@NotNull final SyntaxNode node,
                                final NodeType split,
                                final NodeType createType) {
        for (final var line : new ArrayList<>(node.children())) {
            groupLayer(n -> n.getType() == split,
                       (x, y) -> new SimpleSyntaxNode(createType, x, y, null),
                       line,
                       false);
        }
    }

    /**
     * Create code blocks.
     *
     * @param node parent nodes to create blocks on
     */
    private void buildBlocks(@NotNull final SyntaxNode node) {
        insertLayer(n -> n.children().size() <= 1,
                    (x, y) -> new SimpleSyntaxNode(NodeType.BLOCK, x, y, null), node);
        for (final var n : new ArrayList<>(node.children())) {
            if (calculateSize(n) <= 1) {
                node.removeChild(n);
            }
        }
    }

    /**
     * Calculate size of node recursively.
     *
     * @param node node to calculate size of
     * @return all Leaf node descendants
     */
    private int calculateSize(@NotNull final SyntaxNode node) {
        if (node.children().isEmpty()) {
            return 1;
        }
        int result = 0;
        for (final var n : node.children()) {
            result += calculateSize(n);
        }
        return result;
    }

    /**
     * Edit Layer.
     *
     * @param function    function to apply on children
     * @param parentLayer parentNode
     */
    private void editLayer(@NotNull final Consumer<SyntaxNode> function,
                           @NotNull final SyntaxNode parentLayer) {
        editLayer(n -> true, function, parentLayer);
    }

    /**
     * Edit Layer.
     *
     * @param filter      filter out nodes to edit
     * @param function    function to apply
     * @param parentLayer parentNode
     */
    private void editLayer(@NotNull final Predicate<SyntaxNode> filter,
                           @NotNull final Consumer<SyntaxNode> function,
                           @NotNull final SyntaxNode parentLayer) {
        for (final var n : parentLayer.children()) {
            if (filter.test(n)) {
                function.accept(n);
            }
        }
    }

    /**
     * Split children on Predicate and group into new Node.
     *
     * @param filter      filter to test where to split
     * @param supplier    supplier for new node
     * @param parentNode  parent node
     * @param includeLast whether to include the last group
     */
    private void groupLayer(@NotNull final Predicate<SyntaxNode> filter,
                            @NotNull final BiFunction<Integer, Integer, SyntaxNode> supplier,
                            @NotNull final SyntaxNode parentNode,
                            final boolean includeLast) {
        final SyntaxNode[] nodes = parentNode.children().toArray(new SyntaxNode[0]);
        final List<SyntaxNode> newNodes = group(nodes, filter, supplier, includeLast);
        for (final var n : newNodes) {
            IntStream.rangeClosed(n.getBegin(), n.getEnd()).forEach(i -> n.addChild(nodes[i]));
            parentNode.addChild(n);
        }

    }

    /**
     * Insert new layer by grouping.
     *
     * @param filter      filter to split on
     * @param supplier    supplier for new nodes
     * @param insertAfter parent node of new nodes
     */
    private void insertLayer(@NotNull final Predicate<SyntaxNode> filter,
                             @NotNull final BiFunction<Integer, Integer, SyntaxNode> supplier,
                             @NotNull final SyntaxNode insertAfter) {
        final SyntaxNode[] nodes = insertAfter.children().toArray(new SyntaxNode[0]);
        final List<SyntaxNode> newNodes = group(nodes, filter, supplier, true);
        insertAfter.removeAll();
        for (final var n : newNodes) {
            IntStream.rangeClosed(n.getBegin(), n.getEnd()).forEach(i -> n.addChild(nodes[i]));
        }
        insertAfter.addAll(newNodes);
    }

    /**
     * Group items based on predicate.
     *
     * @param items       items to group
     * @param filter      filter to split on
     * @param supplier    supplier for new nodes
     * @param includeLast whether to include last group
     * @param <T>         Type of input data
     * @param <K>         Type of output data
     * @return List of grouped items
     */
    @NotNull
    private <T, K> List<K> group(@NotNull final T[] items,
                                 @NotNull final Predicate<T> filter,
                                 @NotNull final BiFunction<Integer, Integer, K> supplier,
                                 final boolean includeLast) {
        final List<K> groups = new ArrayList<>();
        final int[] splits = splitOn(items, filter);
        int current = 0;
        for (final int i : splits) {
            groups.add(supplier.apply(current, i));
            current = i + 1;
        }
        if (current < items.length && includeLast) {
            groups.add(supplier.apply(current, items.length - 1));
        }
        return groups;
    }

    /**
     * Get indices of split.
     *
     * @param array     array to split
     * @param predicate predicate to split on
     * @param <T>       Type of input array
     * @return integer array containing split indices
     */
    private <T> int[] splitOn(@NotNull final T[] array, @NotNull final Predicate<T> predicate) {
        final List<Integer> values = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                values.add(i);
            }
        }
        return values.stream().sorted(Integer::compareTo).mapToInt(Integer::intValue).toArray();
    }

    /**
     * Parse {@link SyntaxNode} to {@link SyntaxNode}.
     *
     * @param token token to parse
     * @param index index of token
     * @return Syntax Node
     */
    @NotNull
    private SyntaxNode tokenToNode(@NotNull final SyntaxToken token, final int index) {
        final SyntaxNode node = new LeafNode(NodeType.LEAF, index, null);
        switch (token.getType()) {
            case PUNCTUATION:
                final char c = token.getValue().toString().charAt(0);
                if (c == Punctuation.INSTRUCTION_END) {
                    node.setType(NodeType.INSTRUCTION_END);
                } else if (c == Punctuation.JUMP_DELIMITER) {
                    node.setType(NodeType.JUMP_DEL);
                } else if (c == Punctuation.SCOPE_OPEN) {
                    node.setType(NodeType.SCOPE_OPEN);
                } else if (c == Punctuation.SCOPE_CLOSED) {
                    node.setType(NodeType.SCOPE_CLOSED);
                }
                break;
            case COMMENT:
                node.setType(NodeType.COMMENT);
                break;
            case NEW_LINE:
                node.setType(NodeType.NEW_LINE);
                break;
            default:
                break;
        }
        return node;
    }
}
