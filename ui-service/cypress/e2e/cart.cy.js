describe('Carrinho', () => {
  beforeEach(() => {
    cy.visit('/login')
    cy.get('#login').type('cliente@teste.com')
    cy.get('#password').type('senha123')
    cy.get('form').submit()
  })

  it('deve adicionar veículo ao carrinho', () => {
    cy.get('.vehicle-card').first().find('.btn').click()
    cy.get('.btn').contains('Adicionar ao Carrinho').click()
    cy.url().should('include', '/cart')
    cy.get('.cart-item').should('have.length', 1)
  })

  it('deve exibir mensagem quando carrinho expirar', () => {
    cy.get('.vehicle-card').first().find('.btn').click()
    cy.get('.btn').contains('Adicionar ao Carrinho').click()
    
    // Espera 1 minuto para o carrinho expirar
    cy.wait(60000)
    
    cy.get('.alert-danger')
      .should('be.visible')
      .and('contain', 'Seu carrinho expirou')
  })

  it('deve completar compra com sucesso', () => {
    cy.get('.vehicle-card').first().find('.btn').click()
    cy.get('.btn').contains('Adicionar ao Carrinho').click()
    cy.get('.btn').contains('Efetivar Compra').click()
    
    cy.url().should('include', '/vehicles')
    cy.get('.alert-success')
      .should('be.visible')
      .and('contain', 'Compra realizada com sucesso')
  })
}) 