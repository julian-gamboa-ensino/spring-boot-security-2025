describe('Autenticação', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('deve exibir mensagem de erro para credenciais inválidas', () => {
    cy.get('#login').type('usuario_invalido')
    cy.get('#password').type('senha_invalida')
    cy.get('form').submit()

    cy.get('.alert-danger').should('be.visible')
  })

  it('deve fazer login com sucesso e redirecionar para listagem de veículos', () => {
    cy.get('#login').type('cliente@teste.com')
    cy.get('#password').type('senha123')
    cy.get('form').submit()

    cy.url().should('include', '/vehicles')
    cy.get('.vehicles-grid').should('be.visible')
  })

  it('deve registrar novo usuário com sucesso', () => {
    cy.visit('/register')
    cy.get('#login').type('novo_usuario@teste.com')
    cy.get('#password').type('senha123')
    cy.get('#name').type('Novo Usuário')
    cy.get('#cpf').type('12345678900')
    cy.get('form').submit()

    cy.url().should('include', '/vehicles')
  })
}) 