Cypress.Commands.add('login', (email, password) => {
  cy.visit('/login')
  cy.get('#login').type(email)
  cy.get('#password').type(password)
  cy.get('form').submit()
})

Cypress.Commands.add('addToCart', () => {
  cy.get('.vehicle-card').first().find('.btn').click()
  cy.get('.btn').contains('Adicionar ao Carrinho').click()
}) 