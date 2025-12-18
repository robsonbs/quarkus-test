package com.robsonbs.view;

/**
 * Record imutável representando um item de navegação breadcrumb.
 * 
 * <p>Este componente é utilizado para construir trilhas de navegação
 * (breadcrumbs) nas páginas da aplicação, permitindo ao usuário
 * visualizar sua localização atual na hierarquia de páginas.</p>
 * 
 * <h2>Tipos de Itens</h2>
 * <ul>
 *   <li><strong>Link ativo:</strong> Item clicável que leva a outra página</li>
 *   <li><strong>Item atual:</strong> Representa a página atual (sem link)</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // Criar breadcrumb para página de edição de tarefa
 * List<BreadcrumbItem> breadcrumb = List.of(
 *     BreadcrumbItem.link("Início", "/"),
 *     BreadcrumbItem.link("Tarefas", "/tasks"),
 *     BreadcrumbItem.current("Editar Tarefa")
 * );
 * 
 * // No template Qute
 * // {#for item in breadcrumb}
 * //   {#if item.hasHref}
 * //     <a href="{item.href}">{item.label}</a> /
 * //   {#else}
 * //     <span>{item.label}</span>
 * //   {/if}
 * // {/for}
 * }</pre>
 * 
 * <h2>Integração com Templates</h2>
 * <p>O template {@code includes/breadcrumb.html} renderiza automaticamente
 * a lista de breadcrumbs usando Tailwind CSS para estilização.</p>
 * 
 * @param label texto exibido para o item do breadcrumb
 * @param href  URL de destino ou {@code null} para item atual (não-clicável)
 * 
 * @author Sistema de Navegação
 * @version 1.0
 * @since 1.0
 */
public record BreadcrumbItem(String label, String href) {

    /**
     * Cria um item de breadcrumb clicável com link.
     * 
     * <p>Use para itens que navegam para outras páginas.</p>
     * 
     * @param label texto exibido no link
     * @param href  URL de destino do link
     * @return novo item de breadcrumb com link
     * @see #current(String)
     */
    public static BreadcrumbItem link(String label, String href) {
        return new BreadcrumbItem(label, href);
    }

    /**
     * Cria um item de breadcrumb representando a página atual.
     * 
     * <p>Este item não possui link (href é {@code null}) e representa
     * a localização atual do usuário. Geralmente é o último item
     * na sequência de breadcrumbs.</p>
     * 
     * @param label texto descritivo da página atual
     * @return novo item de breadcrumb sem link
     * @see #link(String, String)
     */
    public static BreadcrumbItem current(String label) {
        return new BreadcrumbItem(label, null);
    }

    /**
     * Verifica se o item possui um link válido.
     * 
     * <p>Útil em templates para determinar se o item deve ser
     * renderizado como link clicável ou texto simples.</p>
     * 
     * @return {@code true} se href não for nulo nem vazio,
     *         {@code false} caso contrário
     */
    public boolean hasHref() {
        return href != null && !href.isBlank();
    }
}
