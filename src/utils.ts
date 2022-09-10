export function isGm(game: Game): boolean {
    return game?.user?.name === 'Gamemaster';
}
