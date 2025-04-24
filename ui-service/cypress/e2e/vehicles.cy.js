describe('Listagem de Veículos', () => {
  beforeEach(() => {
    cy.visit('/login')
    cy.get('#login').type('cliente@teste.com')
    cy.get('#password').type('senha123')
    cy.get('form').submit()
  })

  it('deve listar veículos disponíveis', () => {
    cy.get('.vehicle-card').should('have.length.gt', 0)
  })

  it('deve mostrar detalhes do veículo', () => {
    cy.get('.vehicle-card').first().find('.btn').click()
    cy.url().should('include', '/vehicles/')
    cy.get('.vehicle-details').should('be.visible')
  })
}) 